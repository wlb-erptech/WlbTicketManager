package com.erp.erp.application.customer;

import com.erp.erp.application.dto.response.BillBlock;
import com.erp.erp.application.dto.response.CustomerBillBlock;
import com.erp.erp.application.dto.response.PaymentMini;
import com.erp.erp.application.dto.response.StoreCustomerBillHistoryDto;
import com.erp.erp.application.dto.response.TicketMini;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.client.Store;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.payment.PaymentRepository;
import com.erp.erp.domain.model.ticket.SoldStatus;
import com.erp.erp.domain.model.ticket.SoldStatusRepository;
import com.erp.erp.domain.model.ticket.Ticket;
import com.erp.erp.domain.model.ticket.TicketRepository;
import com.erp.erp.domain.model.user.User;
import com.erp.erp.domain.model.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCustomerBillHistoryService {

  private final SoldStatusRepository soldStatusRepo;
  private final PaymentRepository paymentRepo;
  private final TicketRepository ticketRepo;
  private final UserRepository userRepository;

  /**
   * Store-scoped, grouped by customer (from SoldStatus fields), bills only.
   * No extra filters (no phone/aadhaar/date).
   */
  public StoreCustomerBillHistoryDto getBillHistory(String username, int page, int size) {
    Set<Long> storeIds = getStoreIds(username);
    if (storeIds.isEmpty()) {
      return new StoreCustomerBillHistoryDto(page, size, 0, List.of());
    }

    // 1) Fetch all bills for the stores (lightweight)
    List<SoldStatus> bills = soldStatusRepo.findByStores(storeIds);
    if (bills.isEmpty()) {
      return new StoreCustomerBillHistoryDto(page, size, 0, List.of());
    }

    // 2) Fetch children in two separate queries
    List<Long> billIds = bills.stream().map(SoldStatus::getSoldTableId).toList();

    Map<Long, List<Payment>> paymentsByBill = paymentRepo.findByBillIds(billIds).stream()
        .collect(Collectors.groupingBy(p -> p.getBill().getSoldTableId()));

    Map<Long, List<Ticket>> ticketsByBill = ticketRepo.findByBillIds(billIds).stream()
        .collect(Collectors.groupingBy(t -> t.getBill().getSoldTableId()));

    // 3) Group bills by customer (from SoldStatus)
    record CKey(String phone, String name, String gstNumber, String gstId) {}
    Map<CKey, List<SoldStatus>> billsByCustomer = new LinkedHashMap<>();
    for (SoldStatus s : bills) {
      CKey key = new CKey(ns(s.getPhoneNumber()), ns(s.getCustomerName()), ns(s.getGstNumber()), ns(s.getGstId()));
      billsByCustomer.computeIfAbsent(key, __ -> new ArrayList<>()).add(s);
    }

    // Sort bills inside each customer (date desc, id desc)
    billsByCustomer.values().forEach(list ->
        list.sort(Comparator
            .comparing(SoldStatus::getBillDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
            .thenComparing(SoldStatus::getSoldTableId, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
    );

    // 4) Page by customer groups
    List<CKey> keys = new ArrayList<>(billsByCustomer.keySet());
    int from = Math.min(page * size, keys.size());
    int to   = Math.min(from + size, keys.size());
    List<CKey> pageKeys = keys.subList(from, to);

    // 5) Map to DTOs
    List<CustomerBillBlock> customers = pageKeys.stream().map(k -> {
      List<SoldStatus> custBills = billsByCustomer.getOrDefault(k, List.of());
      List<BillBlock> billBlocks = custBills.stream()
          .map(b -> toBillBlock(
              b,
              paymentsByBill.getOrDefault(b.getSoldTableId(), List.of()),
              ticketsByBill.getOrDefault(b.getSoldTableId(), List.of())
          ))
          .toList();
      return new CustomerBillBlock(k.name, k.phone, k.gstNumber, k.gstId, billBlocks);
    }).toList();

    return new StoreCustomerBillHistoryDto(page, size, keys.size(), customers);
  }

  // ---- Mappers ----

  private BillBlock toBillBlock(SoldStatus s, List<Payment> payments, List<Ticket> tickets) {
    BigDecimal paid   = sum(payments, p -> p.getModeOfPayment() != PaymentMode.CREDIT);
    BigDecimal credit = sum(payments, p -> p.getModeOfPayment() == PaymentMode.CREDIT);

    List<TicketMini> ticketDtos = tickets.stream()
        .map(t -> new TicketMini(t.getTicketId(), t.getProductName(), t.getBrand(), t.getColorSpecs(), t.getRamRomSpecs()))
        .toList();

    List<PaymentMini> paymentDtos = payments.stream()
        .sorted(Comparator
            .comparing(Payment::getPaidAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Payment::getId))
        .map(p -> new PaymentMini(p.getId(), p.getModeOfPayment().name(), p.getAmount(), p.getPaidAt()))
        .toList();

    return new BillBlock(
        s.getSoldTableId(),
        s.getBillNumber(),
        s.getBillDate(),
        s.getStoreId(),
        s.getProfit(),
        ticketDtos,
        paymentDtos,
        paid,
        credit
    );
  }

  // ---- utils ----

  private static BigDecimal sum(List<Payment> items, Predicate<Payment> pred) {
    if (items == null || items.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return items.stream()
        .filter(Objects::nonNull)
        .filter(pred)
        .map(Payment::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static String ns(String s) { return (s == null || s.isBlank()) ? null : s; }

  private Set<Long> getStoreIds(String username) {
    Optional<User> user = userRepository.findByUserEmail(username);
    if (user.isEmpty()) {
      throw new EntityNotFoundException("No User found with your email!");
    }
    return Optional.ofNullable(user.get().getStores())
        .orElseGet(Collections::emptySet)
        .stream()
        .map(Store::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }
}
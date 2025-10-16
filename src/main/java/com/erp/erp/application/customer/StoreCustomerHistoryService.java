package com.erp.erp.application.customer;

import com.erp.erp.application.dto.response.CustomerBlock;
import com.erp.erp.application.dto.response.InvoiceBlock;
import com.erp.erp.application.dto.response.PaymentMini;
import com.erp.erp.application.dto.response.StoreCustomerHistoryDto;
import com.erp.erp.application.dto.response.TicketMini;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.client.Store;
import com.erp.erp.domain.model.invoice.Invoice;
import com.erp.erp.domain.model.invoice.InvoiceRepository;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.payment.PaymentRepository;
import com.erp.erp.domain.model.ticket.Ticket;
import com.erp.erp.domain.model.ticket.TicketRepository;
import com.erp.erp.domain.model.user.User;
import com.erp.erp.domain.model.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreCustomerHistoryService {

  private final InvoiceRepository invoiceRepo;
  private final PaymentRepository paymentRepo; // NEW
  private final TicketRepository ticketRepo;   // NEW
  private final UserRepository userRepository;

  public StoreCustomerHistoryDto getHistory(String username, int page, int size) {
    // 1) All invoices for the user's stores (lightweight — no entity graph)
    Set<Long> storeIds = getStoreIds(username);
    List<Invoice> invoices = storeIds.isEmpty()
        ? List.of()
        : invoiceRepo.findByStore(storeIds); // assuming your repo supports Set<Long>

    if (invoices.isEmpty()) {
      return new StoreCustomerHistoryDto(page, size, 0, List.of());
    }

    // 2) Fetch children in two separate queries, then group by invoiceId
    List<Long> invIds = invoices.stream().map(Invoice::getId).toList();

    Map<Long, List<Payment>> paymentsByInv = paymentRepo.findByInvoiceIds(invIds).stream()
        .collect(Collectors.groupingBy(p -> p.getInvoice().getId()));

    Map<Long, List<Ticket>> ticketsByInv = ticketRepo.findByInvoiceIds(invIds).stream()
        .collect(Collectors.groupingBy(t -> t.getInvoice().getId()));

    // 3) Group invoices by customer key (from invoice fields)
    record CKey(String phone, String aadhar, String name) {}
    Map<CKey, List<Invoice>> invByCustomer = new LinkedHashMap<>();
    for (Invoice i : invoices) {
      CKey key = new CKey(ns(i.getPhoneNumber()), i.getCustomerDocumentId(), ns(i.getCustomerName()));
      invByCustomer.computeIfAbsent(key, __ -> new ArrayList<>()).add(i);
    }

    // Sort each customer's invoices (date desc, id desc)
    invByCustomer.values().forEach(list ->
        list.sort(Comparator
            .comparing(Invoice::getInvoiceDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
            .thenComparing(Invoice::getId, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
    );

    // 4) Page by number of customer groups
    List<CKey> keys = new ArrayList<>(invByCustomer.keySet());
    int from = Math.min(page * size, keys.size());
    int to   = Math.min(from + size, keys.size());
    List<CKey> pageKeys = keys.subList(from, to);

    // 5) Map to DTOs using grouped children (no direct bag access)
    List<CustomerBlock> customers = pageKeys.stream().map(k -> {
      List<Invoice> invs = invByCustomer.getOrDefault(k, List.of());
      List<InvoiceBlock> blocks = invs.stream()
          .map(inv -> toInvoiceBlock(
              inv,
              paymentsByInv.getOrDefault(inv.getId(), List.of()),
              ticketsByInv.getOrDefault(inv.getId(), List.of())
          ))
          .toList();
      return new CustomerBlock(k.name, k.phone, k.aadhar, blocks);
    }).toList();

    return new StoreCustomerHistoryDto(page, size, keys.size(), customers);
  }

  // ---- mapping helpers (use children lists passed in) ----

  private InvoiceBlock toInvoiceBlock(Invoice i,
      List<Payment> payments,
      List<Ticket> tickets) {
    BigDecimal paid   = sum(payments, p -> p.getModeOfPayment() != PaymentMode.CREDIT);
    BigDecimal credit = sum(payments, p -> p.getModeOfPayment() == PaymentMode.CREDIT);
    BigDecimal balance = clampZero(safe(i.getTotalAmount()).subtract(paid).subtract(credit));

    var ticketDtos = tickets.stream()
        .map(t -> new TicketMini(
            t.getTicketId(),
            t.getProductName(),
            t.getBrand(),
            t.getColorSpecs(),
            t.getRamRomSpecs()))
        .toList();

    var paymentDtos = payments.stream()
        .sorted(Comparator
            .comparing(Payment::getPaidAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Payment::getId))
        .map(p -> new PaymentMini(
            p.getId(),
            p.getModeOfPayment().name(),
            p.getAmount(),
            p.getPaidAt()))
        .toList();

    return new InvoiceBlock(
        i.getId(),
        i.getInvoiceNumber(),
        i.getInvoiceDate(),
        i.getStoreId(),
        i.getTotalAmount(),
        paid,
        credit,
        balance,
        ticketDtos,
        paymentDtos
    );
  }

  // ---- utilities ----

  private static String ns(String s) { return (s == null || s.isBlank()) ? null : s; }

  private static BigDecimal safe(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

  private static BigDecimal clampZero(BigDecimal v) {
    if (v == null) return BigDecimal.ZERO;
    return v.signum() < 0 ? BigDecimal.ZERO : v;
  }

  private static BigDecimal sum(List<Payment> list, Predicate<Payment> pred) {
    if (list == null || list.isEmpty()) return BigDecimal.ZERO;
    return list.stream()
        .filter(Objects::nonNull)
        .filter(pred)
        .map(Payment::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

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

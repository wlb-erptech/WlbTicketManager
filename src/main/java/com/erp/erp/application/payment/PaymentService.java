package com.erp.erp.application.payment;

import com.erp.erp.application.dto.response.BillResponseDto;
import com.erp.erp.application.dto.response.InvoiceResponseDto;
import com.erp.erp.application.dto.response.PaymentRequestDto;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.client.Store;
import com.erp.erp.domain.model.invoice.Invoice;
import com.erp.erp.domain.model.invoice.InvoiceRepository;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.payment.PaymentRepository;
import com.erp.erp.domain.model.ticket.SoldStatus;
import com.erp.erp.domain.model.ticket.SoldStatusRepository;
import com.erp.erp.domain.model.ticket.Ticket;
import com.erp.erp.domain.model.ticket.TicketRepository;
import com.erp.erp.domain.model.user.User;
import com.erp.erp.domain.model.user.UserRepository;
import com.erp.erp.infrastructure.utility.BillMapper;
import com.erp.erp.infrastructure.utility.InvoiceMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  private final PaymentRepository repo;
  private final InvoiceRepository invoiceRepository;
  private final SoldStatusRepository soldStatusRepository;
  private final TicketRepository ticketRepository;
  private final UserRepository userRepository;

  public PaymentService(PaymentRepository repo,
      InvoiceRepository invoiceRepository,
      SoldStatusRepository soldStatusRepository,
      TicketRepository ticketRepository,
      UserRepository userRepository) {
    this.repo = repo;
    this.invoiceRepository = invoiceRepository;
    this.soldStatusRepository = soldStatusRepository;
    this.ticketRepository = ticketRepository;
    this.userRepository = userRepository;
  }

//  public List<InvoiceResponseDto> getInvoiceCreditPayments(String username) {
//    Set<Long> storeIds = getStoreIds(username);
//    List<Invoice> invoice = invoiceRepository.findAllWithInvoiceCredits(storeIds);
//    List<InvoiceResponseDto> responseDtos = new ArrayList<>();
//    for (Invoice value : invoice) {
//      responseDtos.add(InvoiceMapper.mapToInvoiceResponseDto(value));
//    }
//    return responseDtos;
//  }
  public List<InvoiceResponseDto> getInvoiceCreditPayments(String username) {
    return invoiceRepository.findAllWithInvoiceCredits(getStoreIds(username)).stream()
        .filter(i -> i.netCredit().signum() > 0)
        .map(InvoiceMapper::mapToInvoiceResponseDto)
        .toList();
  }

//  public List<BillResponseDto> getBillCreditPayments(String username) {
//    Set<Long> storeIds = getStoreIds(username);
//    List<SoldStatus> bills = soldStatusRepository.findAllWithBillCredits(storeIds);
//    List<BillResponseDto> responseDtos = new ArrayList<>();
//    for (SoldStatus value : bills) {
//      responseDtos.add(BillMapper.toDto(value));
//    }
//    return responseDtos;
//  }

  public List<BillResponseDto> getBillCreditPayments(String username) {
    System.out.println("StoreId for username : " + getStoreIds(username));
    System.out.println("SoldStatus : " + soldStatusRepository.findAllWithBillCredits(getStoreIds(username)).get(0).getSoldTableId());
    return soldStatusRepository.findAllWithBillCredits(getStoreIds(username)).stream()
        .filter(b -> b.netCredit().signum() > 0)
        .map(BillMapper::toDto)
        .toList();
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

  @Transactional
  public InvoiceResponseDto addPaymentForInvoice(PaymentRequestDto dto) {
    require(dto.creditType(), "BUY", "Credit Type must be BUY for invoice payments.");
    Long invoiceId = Objects.requireNonNull(dto.invoiceOrBillId(), "No Invoice Id present.");

    Invoice invoice = invoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + invoiceId));

    List<Ticket> tickets = ticketRepository.findByInvoice_Id(invoiceId);

    if (tickets.isEmpty()) {
      throw new EntityNotFoundException("No tickets associated to invoice.");
    }
    boolean anyTicketUpdated = false;
    for (Ticket t : tickets) {
      if (t.getInvoice() == null || !invoiceId.equals(t.getInvoice().getId())) {
        t.setInvoice(invoice);
        anyTicketUpdated = true;
      }
    }
    if (anyTicketUpdated) {
      ticketRepository.saveAll(tickets); // owning side persists the INVOICE_ID
    }

    BigDecimal amount = Objects.requireNonNull(dto.amount(), "Amount is required.");
    if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive.");

    BigDecimal outstanding = invoice.netCredit();
    if (outstanding.signum() <= 0) throw new IllegalStateException("No outstanding credit to repay.");
    if (amount.compareTo(outstanding) > 0) {
      throw new IllegalArgumentException("Repayment exceeds outstanding credit (" + outstanding + ").");
    }

    Payment creditAdj = Payment.builder()
        .modeOfPayment(PaymentMode.CREDIT)
        .amount(amount.negate())
        .paidAt(LocalDate.now())
        .build();
    invoice.addPayment(creditAdj);

    Payment actual = Payment.builder()
        .modeOfPayment(dto.modeOfPayment())
        .transactionId(dto.transactionId())
        .amount(amount)
        .paidAt(LocalDate.now())
        .build();
    invoice.addPayment(actual);
    invoiceRepository.save(invoice);

    return InvoiceMapper.mapToInvoiceResponseDto(invoice);
  }

  @Transactional
  public BillResponseDto addPaymentForBill(PaymentRequestDto dto) {
    require(dto.creditType(), "SELL", "Credit Type must be SELL for invoice payments.");
    Long billId = Objects.requireNonNull(dto.invoiceOrBillId(), "No Bill Id present.");

    SoldStatus bill = soldStatusRepository.findById(billId)
        .orElseThrow(() -> new EntityNotFoundException("Bill not found: " + billId));

    List<Ticket> tickets = ticketRepository.findByBill_SoldTableId(billId);

    if (tickets.isEmpty()) {
      throw new EntityNotFoundException("No tickets associated to bill.");
    }
    boolean anyTicketUpdated = false;
    for (Ticket t : tickets) {
      if (t.getBill() == null || !billId.equals(t.getBill().getSoldTableId())) {
        t.setBill(bill);
        anyTicketUpdated = true;
      }
    }
    if (anyTicketUpdated) {
      ticketRepository.saveAll(tickets);
    }

    BigDecimal amount = Objects.requireNonNull(dto.amount(), "Amount is required.");
    if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive.");

    BigDecimal outstanding = bill.netCredit();
    if (outstanding.signum() <= 0) throw new IllegalStateException("No outstanding credit to repay.");
    if (amount.compareTo(outstanding) > 0) {
      throw new IllegalArgumentException("Repayment exceeds outstanding credit (" + outstanding + ").");
    }

    Payment creditAdj = Payment.builder()
        .modeOfPayment(PaymentMode.CREDIT)
        .amount(amount.negate())
        .paidAt(LocalDate.now())
        .build();
    bill.addPayment(creditAdj);

    Payment actual = Payment.builder()
        .modeOfPayment(dto.modeOfPayment())
        .transactionId(dto.transactionId())
        .amount(amount)
        .paidAt(LocalDate.now())
        .build();
    bill.addPayment(actual);
    soldStatusRepository.save(bill);

    return BillMapper.toDto(bill);
  }

  private static void require(String actual, String expected, String message) {
    if (!Objects.equals(actual, expected)) throw new IllegalArgumentException(message);
  }
}
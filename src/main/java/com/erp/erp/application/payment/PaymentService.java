package com.erp.erp.application.payment;

import com.erp.erp.application.dto.InvoiceProductDto;
import com.erp.erp.application.dto.PaymentDto;
import com.erp.erp.application.dto.response.BillResponseDto;
import com.erp.erp.application.dto.response.InvoiceResponseDto;
import com.erp.erp.application.dto.response.PaymentRequestDto;
import com.erp.erp.application.dto.response.PaymentResponseDto;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.invoice.Invoice;
import com.erp.erp.domain.model.invoice.InvoiceRepository;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.payment.PaymentRepository;
import com.erp.erp.domain.model.ticket.SoldStatus;
import com.erp.erp.domain.model.ticket.SoldStatusRepository;
import com.erp.erp.domain.model.ticket.Ticket;
import com.erp.erp.domain.model.ticket.TicketRepository;
import com.erp.erp.infrastructure.utility.BillMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  private final PaymentRepository repo;
  private final InvoiceRepository invoiceRepository;
  private final SoldStatusRepository soldStatusRepository;
  private final TicketRepository ticketRepository;

  public PaymentService(PaymentRepository repo,
      InvoiceRepository invoiceRepository,
      SoldStatusRepository soldStatusRepository,
      TicketRepository ticketRepository) {
    this.repo = repo;
    this.invoiceRepository = invoiceRepository;
    this.soldStatusRepository = soldStatusRepository;
    this.ticketRepository = ticketRepository;
  }

  public List<InvoiceResponseDto> getInvoiceCreditPayments() {
    List<Invoice> invoice = invoiceRepository.findAllWithInvoiceCredits();
    List<InvoiceResponseDto> responseDtos = new ArrayList<>();
    for (Invoice value : invoice) {
      responseDtos.add(mapToInvoiceResponseDto(value));
    }
    return responseDtos;
  }

  public List<BillResponseDto> getBillCreditPayments() {
    List<SoldStatus> bills = soldStatusRepository.findAllWithBillCredits();
    List<BillResponseDto> responseDtos = new ArrayList<>();
    for (SoldStatus value : bills) {
      responseDtos.add(BillMapper.toDto(value));
    }
    return responseDtos;
  }

  @Transactional
  public InvoiceResponseDto addPaymentForInvoice(PaymentRequestDto dto) {
    Invoice invoice;
    Ticket ticket = null;
    if (Objects.equals(dto.creditType(), "BUY") && dto.invoiceOrBillId() != null) {
      invoice = invoiceRepository.findById(dto.invoiceOrBillId())
          .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + dto.invoiceOrBillId()));
    } else if (Objects.equals(dto.creditType(), "SELL") && dto.invoiceOrBillId() != null) {
      throw new IllegalArgumentException("Credit Type can not be SELL");
    } else if (dto.invoiceOrBillId() == null) {
      throw new IllegalArgumentException("No Invoice or Bill Id present?");
    } else {
      throw new IllegalArgumentException("Credit Type is invalid");
    }
    if (dto.ticketId() != null) {
      ticket = ticketRepository.findById(dto.ticketId())
          .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + dto.ticketId()));
    }

    BigDecimal original = new BigDecimal(String.valueOf(dto.amount()));
    BigDecimal negated = original.negate();
    Payment payment1 = Payment.builder()
        .modeOfPayment(PaymentMode.CREDIT)
        .amount(negated)
        .paidAt(LocalDate.now())
        .invoice(invoice)
        .build();
    repo.save(payment1);
    Payment payment = Payment.builder()
        .modeOfPayment(dto.modeOfPayment())
        .transactionId(dto.transactionId())
        .amount(dto.amount())
        .paidAt(LocalDate.now())
        .invoice(invoice)
        .build();
    repo.save(payment);
    if (Objects.equals(dto.creditType(), "BUY")) {
      if (invoice != null) {
        List<Payment> payments = invoice.getPayments();
        payments.add(payment1);
        payments.add(payment);
        invoice.setPayments(payments);
      }
    } else {
      throw new IllegalArgumentException("Credit Type can be SELL only");
    }
    if (ticket != null) {
      ticket.setInvoice(invoice);
    }

    if (invoice != null) {
      invoiceRepository.save(invoice);
    }
    if (ticket != null) {
      ticketRepository.save(ticket);
    }

    if (invoice != null) {
      invoiceRepository.save(invoice);
      return mapToInvoiceResponseDto(invoice);
    }
    else {
      throw new IllegalArgumentException("No Invoice present");
    }
  }

  @Transactional
  public BillResponseDto addPaymentForBill(PaymentRequestDto dto) {
    SoldStatus bill = null;
    Ticket ticket = null;
    if (Objects.equals(dto.creditType(), "SELL") && dto.invoiceOrBillId() != null) {
      bill = soldStatusRepository.findById(dto.invoiceOrBillId())
          .orElseThrow(() -> new EntityNotFoundException("Bill not found: " + dto.invoiceOrBillId()));
    } else if (Objects.equals(dto.creditType(), "BUY") && dto.invoiceOrBillId() != null) {
      throw new IllegalArgumentException("Credit Type can not be BUY");
    } else if (dto.invoiceOrBillId() == null) {
      throw new IllegalArgumentException("No Invoice or Bill Id present?");
    } else {
      throw new IllegalArgumentException("Credit Type is invalid");
    }
    if (dto.ticketId() != null) {
      ticket = ticketRepository.findById(dto.ticketId())
          .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + dto.ticketId()));
    }

    BigDecimal original = new BigDecimal(String.valueOf(dto.amount()));
    BigDecimal negated = original.negate();
    Payment payment1 = Payment.builder()
        .modeOfPayment(PaymentMode.CREDIT)
        .amount(negated)
        .paidAt(LocalDate.now())
        .bill(bill)
        .build();
    repo.save(payment1);
    Payment payment = Payment.builder()
        .modeOfPayment(dto.modeOfPayment())
        .transactionId(dto.transactionId())
        .amount(dto.amount())
        .paidAt(LocalDate.now())
        .bill(bill)
        .build();
    repo.save(payment);
    if (Objects.equals(dto.creditType(), "SELL")) {
      if (bill != null) {
        List<Payment> payments = bill.getPayments();
        payments.add(payment1);
        payments.add(payment);
        bill.setPayments(payments);
      }
    } else {
      throw new IllegalArgumentException("Credit Type can be BUY or SELL only");
    }
    if (ticket != null) {
      ticket.setBill(bill);
    }
    if (bill != null) {
      soldStatusRepository.save(bill);
    }
    if (ticket != null) {
      ticketRepository.save(ticket);
    }

    if (bill != null) {
      soldStatusRepository.save(bill);
      return BillMapper.toDto(bill);
    }
    else {
      throw new IllegalArgumentException("No Invoice present");
    }
  }

  private InvoiceResponseDto mapToInvoiceResponseDto(Invoice invoice) {
    BigDecimal remainingCredit = invoice.getPayments().stream()
        .filter(payment -> payment.getModeOfPayment() == PaymentMode.CREDIT)
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return InvoiceResponseDto.builder()
        .invoiceId(invoice.getId())
        .invoiceNumber(invoice.getInvoiceNumber())
        .invoiceDate(invoice.getInvoiceDate())
        .remainingCredit(remainingCredit)
        .storeId(invoice.getStoreId())
        .phoneNumber(invoice.getPhoneNumber())
        .customerAadharId(invoice.getCustomerAadharId())
        .totalAmount(invoice.getTotalAmount())
        .gstNumber(invoice.getGstNumber())
        .payments(invoice.getPayments().stream().map(p -> PaymentDto.builder()
            .modeOfPayment(p.getModeOfPayment())
            .amount(p.getAmount())
            .transactionId(p.getTransactionId())
            .paidAt(p.getPaidAt())
            .build()).toList())
        .products(invoice.getTickets().stream().map(t -> InvoiceProductDto.builder()
            .ticketId(t.getTicketId())
            .itemId(t.getItemId())
            .productName(t.getProductName())
            .brand(t.getBrand())
            .ramRomSpecs(t.getRamRomSpecs())
            .colorSpecs(t.getColorSpecs())
            .acquisitionCost(t.getAcquisitionCost())
            .refurbishedCost(t.getRefurbishedCost())
            .imeiNo(t.getImeiNo())
            .serialNo(t.getItemSerialNo())
            .boxFlag(t.getBoxFlag())
            .chargerFlag(t.getChargerFlag())
            .sealedFlag(t.getSealedFlag())
            .warranty(t.getWarranty())
            .build()).toList())
        .build();
  }
}
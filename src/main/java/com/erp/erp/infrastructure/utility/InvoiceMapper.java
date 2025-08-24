package com.erp.erp.infrastructure.utility;

import com.erp.erp.application.dto.InvoiceProductDto;
import com.erp.erp.application.dto.PaymentDto;
import com.erp.erp.application.dto.response.InvoiceResponseDto;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.invoice.Invoice;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.ticket.Ticket;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvoiceMapper {

  public static InvoiceResponseDto toDto(Invoice invoice) {
    if (invoice == null) {
      return null;
    }

    Optional<Ticket> optionalTicket = invoice.getTickets() != null && !invoice.getTickets().isEmpty()
        ? Optional.of(invoice.getTickets().get(0))
        : Optional.empty();

    return InvoiceResponseDto.builder()
        .phoneNumber(optionalTicket.map(Ticket::getPhoneNumber).orElse(null))
        .customerName(optionalTicket.map(Ticket::getCustomerName).orElse(null))
        .gstNumber(invoice.getGstNumber())
        .gstId(invoice.getGstId())
        .customerAadharId(optionalTicket.map(Ticket::getCustomerAadharId).orElse(null))
        .storeId(optionalTicket.map(ticket -> ticket.getStore().getId()).orElse(null))
        .payments(invoice.getPayments() != null
            ? invoice.getPayments().stream()
            .map(PaymentMapper::toDto)
            .collect(Collectors.toList())
            : null
        )
        .invoiceDate(invoice.getInvoiceDate())
        .invoiceNumber(invoice.getInvoiceNumber())
        .build();
  }

  public static InvoiceResponseDto mapToInvoiceResponseDto(Invoice invoice) {
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
        .customerName(invoice.getCustomerName())
        .gstId(invoice.getGstId())
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

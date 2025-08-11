package com.erp.erp.infrastructure.utility;

import com.erp.erp.application.dto.response.InvoiceResponseDto;
import com.erp.erp.domain.model.invoice.Invoice;
import com.erp.erp.domain.model.ticket.Ticket;
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

}

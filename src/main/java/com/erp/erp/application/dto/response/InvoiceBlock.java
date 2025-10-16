package com.erp.erp.application.dto.response;

public record InvoiceBlock(
    Long invoiceId, String invoiceNumber, java.time.LocalDate invoiceDate, Long storeId,
    java.math.BigDecimal totalAmount, java.math.BigDecimal totalPaid, java.math.BigDecimal creditApplied,
    java.math.BigDecimal balanceDue,
    java.util.List<TicketMini> tickets,  // all tickets on the invoice (no store filter)
    java.util.List<PaymentMini> payments
) {}
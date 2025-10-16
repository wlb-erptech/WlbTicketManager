package com.erp.erp.application.dto.response;

public record BillBlock(
    Long billId,
    String billNumber,
    java.time.LocalDate billDate,
    Long storeId,
    java.math.BigDecimal profit,                 // optional summary field you have
    java.util.List<TicketMini> tickets,          // tickets linked to the bill (no store filtering needed)
    java.util.List<PaymentMini> payments,        // payments linked to the bill
    java.math.BigDecimal totalPaid,              // non-credit
    java.math.BigDecimal creditApplied           // credit
    // add balance if you later store bill total
) {}
package com.erp.erp.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponseDto(
    String creditType,
    Long paymentId,
    BigDecimal amount,
    LocalDate paidAt,
    String customerName,
    Long invoiceOrBillId,
    Long ticketId
) {}

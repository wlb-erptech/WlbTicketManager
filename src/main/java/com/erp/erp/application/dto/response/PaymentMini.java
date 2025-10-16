package com.erp.erp.application.dto.response;

public record PaymentMini(
    Long paymentId,
    String mode,            // PaymentMode enum name
    java.math.BigDecimal amount,
    java.time.LocalDate paidAt
) {}
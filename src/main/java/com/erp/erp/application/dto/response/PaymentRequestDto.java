package com.erp.erp.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.erp.erp.domain.enums.PaymentMode;

public record PaymentRequestDto(
    String creditType,
    Long id,
    Long invoiceOrBillId,
    Long ticketId,
    PaymentMode modeOfPayment,
    String customerName,
    BigDecimal amount,
    String transactionId
) {}

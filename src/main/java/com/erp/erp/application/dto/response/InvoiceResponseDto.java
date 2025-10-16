package com.erp.erp.application.dto.response;

import com.erp.erp.application.dto.InvoiceProductDto;
import com.erp.erp.application.dto.PaymentDto;
import com.erp.erp.domain.enums.CustomerIdType;
import com.erp.erp.domain.enums.PaymentMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

/**
 * Data Transfer Object for Invoice Data
 */
@Builder
public record InvoiceResponseDto(
    Long invoiceId,
    String phoneNumber,
    String customerName,
    BigDecimal remainingCredit,
    String gstNumber,
    String gstId,
    CustomerIdType documentType,
    String customerDocumentId,
    Long storeId,
    List<PaymentDto> payments,
    LocalDate invoiceDate,
    String invoiceNumber,
    BigDecimal totalAmount,
    List<InvoiceProductDto> products

) {

  @Override
  public String toString() {
    return "TicketDto{" +
        ", phoneNumber='" + phoneNumber + '\'' +
        ", customerName='" + customerName + '\'' +
        ", gstNumber='" + gstNumber + '\'' +
        ", gstId='" + gstId + '\'' +
        '}';
  }
}

package com.erp.erp.application.dto;

import com.erp.erp.domain.enums.CustomerIdType;
import com.erp.erp.domain.enums.PaymentMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

/**
 * Data Transfer Object for Bill entity.
 */
@Builder
public record BillDto(
    Long clientId,
    String phoneNumber,
    Long storeId,
    String customerName,
    CustomerIdType documentType,
    String customerDocumentId,
    String gstId,
    String onlineTrxId,
    String placeOfSale,
    BigDecimal profit,
    String billNumber,
    LocalDate billDate,
    String gstNumber,
    List<PaymentDto> payments

) {

  @Override
  public String toString() {
    return "BillDto";
  }
}

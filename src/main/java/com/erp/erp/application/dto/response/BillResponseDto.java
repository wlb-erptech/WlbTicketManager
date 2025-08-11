package com.erp.erp.application.dto.response;

import com.erp.erp.application.dto.InvoiceProductDto;
import com.erp.erp.application.dto.PaymentDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BillResponseDto {

    private Long billId;
    private String billNumber;
    private LocalDate billDate;
    private Long clientId;
    private String customerName;
    private String phoneNumber;
    private BigDecimal remainingCredit;
    private String gstNumber;
    private String gstId;
    private String onlineTrxId;
    private String placeOfSale;
    private BigDecimal profit;
    private List<PaymentDto> payments;
    private List<InvoiceProductDto> products;

}

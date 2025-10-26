package com.erp.erp.application.dto;// in com.erp.erp.web.dto

import java.math.BigDecimal;
import java.util.List;

public record CartItemDetailDTO(
    Long id,
    String itemSerialNo,
    String imeiNo,
    String batteryHealth,
    String warranty,
    String boxFlag,
    String chargerFlag,
    String sealedFlag,
    String invoiceFlag,
    BigDecimal acquisitionCost,
    BigDecimal refurbishedCost,
    BigDecimal sellingCost,
    String ramRomSpecs,
    String colorSpecs,
    String comments,
    String productName,
    String brand
) {}

package com.erp.erp.application.dto;

import java.math.BigDecimal;

public record CartItemDetailUpdateDto(
    Long id,
    BigDecimal acquisitionCost,
    BigDecimal refurbishedCost,
    String ramRomSpecs,
    String colorSpecs,
    BigDecimal sellingCost
) {}
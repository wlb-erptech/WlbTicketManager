package com.erp.erp.application.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AddBuyItemRequest {
  private Long itemId;
  private Integer quantity = 1;
  private String itemSerialNo;
  private String imeiNo;
  private String batteryHealth;
  private String warranty;
  private String boxFlag;
  private String chargerFlag;
  private String sealedFlag;
  private String invoiceFlag;
  private BigDecimal acquisitionCost;
  private BigDecimal refurbishedCost;
  private String ramRomSpecs;
  private String colorSpecs;
  private String comments;
  private String productName;
  private String brand;
  private String cartType;
}

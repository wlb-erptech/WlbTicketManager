package com.erp.erp.domain.model.item;

import com.erp.erp.domain.model.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "WHITELABEL_CART_ITEM_DETAIL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CartItemDetail extends AbstractEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "ticketSeqGen")
  @TableGenerator(
      name           = "ticketSeqGen",
      table          = "global_sequence",
      pkColumnName   = "seq_name",
      valueColumnName= "next_val",
      pkColumnValue  = "ticket_seq",
      initialValue   = 100000,
      allocationSize = 1
  )
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "CART_ITEM_ID")
  private CartItem cartItem;

  @Column(name = "ITEM_SERIAL_NO")
  private String itemSerialNo;

  @Column(name = "IMEI_NO")
  private String imeiNo;

  @Column(name = "BATTERY_HEALTH")
  private String batteryHealth;

  @Column(name = "WARRANTY")
  private String warranty;

  @Column(name = "BOX_FLAG")
  private String boxFlag;

  @Column(name = "CHARGER_FLAG")
  private String chargerFlag;

  @Column(name = "SEALED_FLAG")
  private String sealedFlag;

  @Column(name = "INVOICE_FLAG")
  private String invoiceFlag;

  @Column(name = "ACQUISITION_COST")
  private BigDecimal acquisitionCost;

  @Column(name = "REFURBISHED_COST")
  private BigDecimal refurbishedCost;

  @Column(name = "SELLING_COST")
  private BigDecimal sellingCost;

  @Column(name = "INTERNAL_MEMORY")
  private String ramRomSpecs;

  @Column(name = "COLOR")
  private String colorSpecs;

  @Column(name = "COMMENT", length = 5000)
  private String comment;

  @Column(name = "PRODUCT_NAME")
  private String productName;

  @Column(name = "BRAND", length = 100)
  private String brand;
}

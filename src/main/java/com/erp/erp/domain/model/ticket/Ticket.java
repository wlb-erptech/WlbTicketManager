package com.erp.erp.domain.model.ticket;

import com.erp.erp.domain.enums.CustomerIdType;
import com.erp.erp.domain.enums.TicketStatus;
import com.erp.erp.domain.model.client.Store;
import com.erp.erp.domain.model.invoice.Invoice;
import com.erp.erp.domain.model.shared.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "WHITELABEL_TICKET")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Ticket extends AbstractEntity {

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
  @Column(name = "TICKET_ID", nullable = false)
  private Long ticketId;

  @Column(name = "CLIENT_ID", nullable = false)
  @NotNull
  private Long clientId;

  @Enumerated(EnumType.STRING)
  @Column(name = "TICKET_STATUS", nullable = false, length = 20)
  @NotNull
  private TicketStatus ticketStatus;

  @Column(name = "PHONE_NUMBER", length = 20)
  private String phoneNumber;

  @Column(name = "CUSTOMER_NAME", length = 100)
  private String customerName;

  @Column(name = "PRODUCT_PURCHASE_TYPE", length = 50)
  private String productPurchaseType;

  @Column(name = "ACQUISITION_COST")
  private BigDecimal acquisitionCost;

  @Column(name = "REFURBISHED_COST")
  private BigDecimal refurbishedCost;

  @Enumerated(EnumType.STRING)
  @Column(name = "CUSTOMER_ID_TYPE", length = 32)
  @NotNull
  private CustomerIdType type;

  @Column(name = "CUSTOMER_DOCUMENT_ID")
  private String customerDocumentId;

  @Column(name = "ITEM_ID", nullable = false)
  private Long itemId;

  @Column(name = "BRAND", length = 100)
  private String brand;

  @Column(name = "USER_EMAIL", nullable = false)
  private String userEmail;

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

  @Column(name = "INTERNAL_MEMORY")
  private String ramRomSpecs;

  @Column(name = "COLOR")
  private String colorSpecs;

  @Column(name = "COMMENT", length = 5000)
  private String comment;

  @Column(name = "PRODUCT_NAME")
  private String productName;

  @OneToMany(
      mappedBy = "ticket",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  @JsonIgnore
  @Builder.Default
  private List<TicketLifecycle> lifecycles = new ArrayList<>();

  @Column(name = "IS_DELETED", length = 1)
  private String isDeleted;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "INVOICE_ID")
  private Invoice invoice;


  @ManyToOne(fetch =
      FetchType.LAZY, optional = false)
  @JoinColumn(name = "STORE_ID")
  @NotNull
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SOLD_TABLE_ID")
  private SoldStatus bill;


//  public BigDecimal totalPaid() {
//    return payments.stream()
//        .map(Payment::getAmount)
//        .reduce(BigDecimal.ZERO, BigDecimal::add);
//  }

//  public boolean isFullyPaid() {
//    BigDecimal invoiceTotal =
//        acquisitionCost.add(refurbishedCost);
//    return totalPaid().compareTo(invoiceTotal) >= 0;
//  }

}

package com.erp.erp.domain.model.invoice;

import com.erp.erp.domain.enums.CustomerIdType;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.ticket.Ticket;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "WHITELABEL_INVOICE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Invoice {

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
  @Column(name = "INVOICE_ID", nullable = false)
  private Long id;

  @Column(name = "PHONE_NUMBER", nullable = false)
  private String phoneNumber;

  @Column(name = "CUSTOMER_NAME", nullable = false)
  private String customerName;

  @Enumerated(EnumType.STRING)
  @Column(name = "CUSTOMER_ID_TYPE", length = 32)
  @NotNull
  private CustomerIdType type;

  @Column(name = "CUSTOMER_DOCUMENT_ID")
  private String customerDocumentId;

  @Column(name = "STORE_ID", nullable = false)
  private Long storeId;

  @Column(name = "INVOICE_NUMBER", nullable = false, unique = true, length = 300)
  private String invoiceNumber;

  @Column(name = "INVOICE_DATE", nullable = false)
  private LocalDate invoiceDate;

  @Column(name = "TOTAL_AMOUNT", precision = 12, scale = 2, nullable = false)
  private BigDecimal totalAmount;

  @Column(name = "GST_NUMBER")
  private String gstNumber;

  @Column(name = "GST_ID")
  private String gstId;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
  private List<Ticket> tickets;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Payment> payments = new ArrayList<>();

  public void addPayment(Payment p) {
    p.setInvoice(this);
    this.payments.add(p);
  }

  public BigDecimal netCredit() {
    return payments == null ? BigDecimal.ZERO
        : payments.stream()
            .filter(p -> p.getModeOfPayment() == PaymentMode.CREDIT)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}

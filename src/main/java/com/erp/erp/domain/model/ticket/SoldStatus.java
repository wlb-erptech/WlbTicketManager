package com.erp.erp.domain.model.ticket;

import com.erp.erp.domain.enums.CustomerIdType;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.shared.AbstractEntity;
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

/**
 * JPA entity mapping for the WHITELABEL_SOLD_STATUS_TABLE.
 */
@Entity
@Table(name = "WHITELABEL_SOLD_STATUS_TABLE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SoldStatus extends AbstractEntity {

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
    @Column(name = "SOLD_TABLE_ID", nullable = false)
    private Long soldTableId;

    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Column(name = "STORE_ID", nullable = false)
    private Long storeId;

    @Column(name = "CUSTOMER_NAME", nullable = false)
    private String customerName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "CUSTOMER_ID_TYPE", length = 32)
    @Builder.Default
    private CustomerIdType type = CustomerIdType.NO_DOC;

    @Column(name = "CUSTOMER_DOCUMENT_ID")
    private String customerDocumentId;

    @Column(name = "PHONE_NUMBER", nullable = false)
    private String phoneNumber;

    @Column(name = "GST_NUMBER")
    private String gstNumber;

    @Column(name = "GST_ID")
    private String gstId;

    @Column(name = "ONLINE_TRX_ID")
    private String onlineTrxId;

    @Column(name = "PLACE_OF_SALE")
    private String placeOfSale;

    @Column(name = "PROFIT")
    private BigDecimal profit;

    @Column(name = "BILL_NUMBER", nullable = false)
    private String billNumber;

    @Column(name = "BILL_DATE", nullable = false)
    private LocalDate billDate;

    @Column(name = "IS_DELTED", nullable = false, length = 1)
    private String isDeleted;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    public void addPayment(Payment p) {
        p.setBill(this);
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

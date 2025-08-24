package com.erp.erp.domain.model.item;

import com.erp.erp.domain.model.shared.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "WHITELABEL_CART")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cart extends AbstractEntity {
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

  @Column(name = "USER_EMAIL", nullable = false)
  private String userEmail;

  @Column(name = "CART_TYPE", nullable = false)
  private String cartType;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  @Builder.Default
  private List<CartItem> items = new ArrayList<>();
}

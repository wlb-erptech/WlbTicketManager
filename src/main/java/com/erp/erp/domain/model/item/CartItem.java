package com.erp.erp.domain.model.item;

import com.erp.erp.domain.model.shared.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "WHITELABEL_CART_ITEM")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CartItem extends AbstractEntity {
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
  @JoinColumn(name = "CART_ID")
  @JsonBackReference
  private Cart cart;

  @Column(name = "ITEM_ID", nullable = false)
  private Long itemId;

  @Column(name = "QUANTITY", nullable = false)
  private Integer quantity;

  @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CartItemDetail> details = new ArrayList<>();
}

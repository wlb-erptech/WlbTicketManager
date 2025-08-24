package com.erp.erp.application.item;

import com.erp.erp.application.dto.AddBuyItemRequest;
import com.erp.erp.application.dto.CartItemDTO;
import com.erp.erp.application.dto.CartItemDetailDTO;
import com.erp.erp.domain.model.item.Cart;
import com.erp.erp.domain.model.item.CartItem;
import com.erp.erp.domain.model.item.CartItemDetail;
import com.erp.erp.domain.model.item.CartItemDetailRepository;
import com.erp.erp.domain.model.item.CartItemRepository;
import com.erp.erp.domain.model.item.CartRepository;
import com.erp.erp.domain.model.ticket.Ticket;
import com.erp.erp.domain.model.ticket.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
  private final CartRepository cartRepo;
  private final CartItemRepository cartItemRepository;
  private final CartItemDetailRepository cartItemDetailRepository;
  private final TicketRepository ticketRepository;

  /** Get buy cart for a user */
  public Page<CartItemDTO> getBuyCartAsPage(String userEmail, Pageable pageable) {
    Optional<Cart> cart = cartRepo.findByUserEmailAndCartType(userEmail, "BUY");
    if (cart.isEmpty()) {
      throw new EntityNotFoundException();
    }
    Page<CartItem> page =  cartItemRepository.findByCartId(cart.get().getId(), pageable);
    return page.map(ci -> {
      List<CartItemDetailDTO> detailDtos = ci.getDetails().stream()
          .map(d -> new CartItemDetailDTO(
              d.getId(),
              d.getItemSerialNo(),
              d.getImeiNo(),
              d.getBatteryHealth(),
              d.getWarranty(),
              d.getBoxFlag(),
              d.getChargerFlag(),
              d.getSealedFlag(),
              d.getInvoiceFlag(),
              d.getAcquisitionCost(),
              d.getRefurbishedCost(),
              d.getRamRomSpecs(),
              d.getColorSpecs(),
              d.getComment(),
              d.getProductName(),
              d.getBrand()
          ))
          .toList();

      return new CartItemDTO(
          ci.getId(),
          ci.getItemId(),
          ci.getQuantity(),
          detailDtos
      );
    });
  }

  /** Get buy cart for a user */
  public Cart getBuyCart(String userEmail) {
    Optional<Cart> cart = cartRepo.findByUserEmailAndCartType(userEmail, "BUY");
    if (cart.isEmpty()) {
      throw new EntityNotFoundException();
    }
    return cart.get();
  }

  /** Get buy cart for a user */
  public Cart getSellCart(String userEmail) {
    Optional<Cart> cart = cartRepo.findByUserEmailAndCartType(userEmail, "SELL");
    if (cart.isEmpty()) {
      throw new EntityNotFoundException();
    }
    return cart.get();
  }

  /** Get sell cart for a user */
  public Page<CartItemDTO> getSellCartAsPage(String userEmail, Pageable pageable) {
    Optional<Cart> cart = cartRepo.findByUserEmailAndCartType(userEmail, "SELL");
    if (cart.isEmpty()) {
      throw new EntityNotFoundException();
    }
    Page<CartItem> page = cartItemRepository.findByCartId(cart.get().getId(), pageable);

    return page.map(ci -> {
      // CHANGED: build detailDtos via two paths (existing details OR hydrate from Ticket)
      List<CartItemDetailDTO> detailDtos;

      if (ci.getDetails() != null && !ci.getDetails().isEmpty()) {
        // unchanged path: existing persisted details
        detailDtos = ci.getDetails().stream()
            .map(d -> new CartItemDetailDTO(
                d.getId(),
                d.getItemSerialNo(),
                d.getImeiNo(),
                d.getBatteryHealth(),
                d.getWarranty(),
                d.getBoxFlag(),
                d.getChargerFlag(),
                d.getSealedFlag(),
                d.getInvoiceFlag(),
                d.getAcquisitionCost(),
                d.getRefurbishedCost(),
                d.getRamRomSpecs(),
                d.getColorSpecs(),
                d.getComment(),
                d.getProductName(),
                d.getBrand()
            ))
            .toList();
      } else {
        // ADDED: SELL path — hydrate from Ticket since itemId holds the ticketId
        Ticket t = ticketRepository.findById(ci.getItemId())
            .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + ci.getItemId())); // ADDED

        detailDtos = List.of( // ADDED
            new CartItemDetailDTO(
                null,                                // no CartItemDetail row persisted  // ADDED
                t.getItemSerialNo(),                // ADDED
                t.getImeiNo(),                      // ADDED
                t.getBatteryHealth(),               // ADDED
                t.getWarranty(),                    // ADDED
                t.getBoxFlag(),                     // ADDED
                t.getChargerFlag(),                 // ADDED
                t.getSealedFlag(),                  // ADDED
                t.getInvoiceFlag(),                 // ADDED
                t.getAcquisitionCost(),             // ADDED
                t.getRefurbishedCost(),             // ADDED
                t.getRamRomSpecs(),                 // ADDED
                t.getColorSpecs(),                  // ADDED
                t.getComment(),                     // ADDED
                t.getProductName(),                 // ADDED
                t.getBrand()                        // ADDED
            )
        );
      }

      // unchanged: assemble CartItemDTO
      return new CartItemDTO(
          ci.getId(),
          ci.getItemId(),
          ci.getQuantity(),
          detailDtos
      );
    });
  }


  /** Get cart for a user */
  public Cart getCart(String userEmail) {
    Optional<Cart> cart = cartRepo.findByUserEmail(userEmail);
    if (cart.isEmpty()) {
      throw new EntityNotFoundException();
    }
    return cart.get();
  }

  /** Get or create the buy cart for a user */
  public Cart getOrCreateBuyCart(String userEmail) {
    return cartRepo.findByUserEmailAndCartType(userEmail, "BUY")
        .orElseGet(() -> {
          Cart cart = Cart.builder()
              .userEmail(userEmail)
              .cartType("BUY")
              .items(new ArrayList<>())
              .build();
          return cartRepo.save(cart);
        });
  }

  /** Get or create the sell cart for a user */
  public Cart getOrCreateSellCart(String userEmail) {
    return cartRepo.findByUserEmailAndCartType(userEmail, "SELL")
        .orElseGet(() -> {
          Cart cart = Cart.builder()
              .userEmail(userEmail)
              .cartType("SELL")
              .items(new ArrayList<>())
              .build();
          return cartRepo.save(cart);
        });
  }

  /** Add an item (or increment quantity) */
  @Transactional
  public Cart addBuyItem(String userEmail, AddBuyItemRequest req) {
    Cart cart = getOrCreateBuyCart(userEmail);

    CartItem item = cart.getItems().stream()
        .filter(ci -> ci.getItemId().equals(req.getItemId()))
        .findFirst()
        .orElseGet(() -> {
          CartItem ci = CartItem.builder()
              .cart(cart)
              .itemId(req.getItemId())
              .quantity(0)
              .details(new ArrayList<>())
              .build();
          cart.getItems().add(ci);
          return ci;
        });

    item.setQuantity(item.getQuantity() + req.getQuantity());

    for (int i = 0; i < req.getQuantity(); i++) {
      CartItemDetail detail = CartItemDetail.builder()
          .cartItem(item)
          .itemSerialNo(req.getItemSerialNo())
          .imeiNo(req.getImeiNo())
          .batteryHealth(req.getBatteryHealth())
          .warranty(req.getWarranty())
          .boxFlag(req.getBoxFlag())
          .chargerFlag(req.getChargerFlag())
          .sealedFlag(req.getSealedFlag())
          .invoiceFlag(req.getInvoiceFlag())
          .acquisitionCost(req.getAcquisitionCost())
          .refurbishedCost(req.getRefurbishedCost())
          .ramRomSpecs(req.getRamRomSpecs())
          .colorSpecs(req.getColorSpecs())
          .comment(req.getComment())
          .productName(req.getProductName())
          .brand(req.getBrand())
          .build();
      item.getDetails().add(detail);
    }
    return cartRepo.save(cart);
  }

  @Transactional
  public Cart addSellItem(String userEmail, List<Long> ticketIds) {
    Cart sellCart = getOrCreateSellCart(userEmail);

    Set<Long> existingTicketIds = sellCart.getItems().stream()
        .map(CartItem::getItemId)
        .collect(Collectors.toSet());

    for (Long ticketId : ticketIds) {
      if (!existingTicketIds.contains(ticketId)) {
        CartItem item = CartItem.builder()
            .cart(sellCart)
            .itemId(ticketId)
            .quantity(1)
            .details(new ArrayList<>())
            .build();
        sellCart.getItems().add(item);
      }
    }
    return cartRepo.save(sellCart);
  }


  /** Clear all items after checkout */
  @Transactional
  public void clearCart(Cart cart) {
    cart.getItems().clear();
    cartRepo.save(cart);
  }

  @Transactional
  public void deleteItemForBuyCart(String userEmail, Long detailId) {
    CartItemDetail detail = cartItemDetailRepository.findById(detailId)
        .orElseThrow(() -> new EntityNotFoundException("Detail not found: " + detailId));
    CartItem item = detail.getCartItem();
    Cart cart = item.getCart();
    if (!cart.getUserEmail().equalsIgnoreCase(userEmail)) {
      throw new AccessDeniedException("Cannot remove detail from another user’s cart");
    }
    item.getDetails().remove(detail);
    item.setQuantity(item.getQuantity() - 1);
    if (item.getQuantity() <= 0) {
      cart.getItems().remove(item);
    }
    cartRepo.save(cart);
  }

  @Transactional
  public void removeFromSellCart(String userEmail, Long ticketId) {
    Cart sellCart = getOrCreateSellCart(userEmail);

    Iterator<CartItem> iterator = sellCart.getItems().iterator();
    while (iterator.hasNext()) {
      CartItem item = iterator.next();
      if (item.getItemId().equals(ticketId)) {
        iterator.remove();
        break;
      }
    }
  }


}

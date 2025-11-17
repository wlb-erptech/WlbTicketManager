package com.erp.erp.application.item;

import com.erp.erp.application.dto.AddBuyItemRequest;
import com.erp.erp.application.dto.AddTicketsToSellCartRequest;
import com.erp.erp.application.dto.CartItemDTO;
import com.erp.erp.application.dto.CartItemDetailDTO;
import com.erp.erp.application.dto.CartItemDetailUpdateDto;
import com.erp.erp.application.dto.CartItemPatchRequest;
import com.erp.erp.domain.enums.TicketStatus;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
              d.getSellingCost(),
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
                d.getSellingCost(),
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
                t.getRefurbishedCost(),
                null,
                t.getRamRomSpecs(),// ADDED
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
          .comment(req.getComments())
          .productName(req.getProductName())
          .brand(req.getBrand())
          .build();
      item.getDetails().add(detail);
    }
    return cartRepo.save(cart);
  }

//  @Transactional
//  public Cart addSellItem(String userEmail, AddTicketsToSellCartRequest req) {
//    Cart sellCart = getOrCreateSellCart(userEmail);
//
//    Set<Long> existingTicketIds = sellCart.getItems().stream()
//        .map(CartItem::getItemId)
//        .collect(Collectors.toSet());
//
//    Set<Long> existingListedTicketIds = ticketRepository.findListedIds(existingTicketIds);
//
//
//    for (Long ticketId : req.getTicketIds()) {
//      if (!existingListedTicketIds.contains(ticketId)) {
//        CartItem item = CartItem.builder()
//            .cart(sellCart)
//            .itemId(ticketId)
//            .quantity(1)
//            .details(new ArrayList<>())
//            .build();
//        sellCart.getItems().add(item);
//      }
//    }
//    return cartRepo.save(sellCart);
//  }

  @Transactional
  public Cart addSellItem(String userEmail, AddTicketsToSellCartRequest req) {
    Cart sellCart = getOrCreateSellCart(userEmail);

    // gather what's already in the cart (avoid duplicates)
    Set<Long> existingTicketIds = sellCart.getItems().stream()
        .map(CartItem::getItemId)
        .collect(Collectors.toSet());

    // nothing to add?
    if (req.getTicketIds() == null || req.getTicketIds().isEmpty()) {
      return sellCart;
    }

    // keep only LISTED ticket ids from the incoming set
    Set<Long> requestedIds = new HashSet<>(req.getTicketIds()); // defensive copy
    Set<Long> listedRequestedIds = ticketRepository.findListedIds(requestedIds); // JPQL you already have

    // compute ids we actually need to add (listed AND not in cart already)
    listedRequestedIds.removeAll(existingTicketIds);
    if (listedRequestedIds.isEmpty()) {
      return sellCart; // all requested tickets are either not LISTED or already present
    }

    // load all tickets in ONE go so we can populate details
    List<Ticket> ticketsToAdd = ticketRepository.findAllById(listedRequestedIds);

    for (Ticket t : ticketsToAdd) {
      // Optional extra guards (comment out if not needed)
      if (t.getTicketStatus() != TicketStatus.LISTED) continue;
      if ("Y".equalsIgnoreCase(t.getIsDeleted())) continue;

      // Build the cart item
      CartItem item = CartItem.builder()
          .cart(sellCart)        // owning side for CartItem
          .itemId(t.getTicketId()) // SELL cart uses ticketId as itemId
          .quantity(1)
          .build();

      // Populate CartItemDetail from Ticket using the builder
      CartItemDetail detail = CartItemDetail.builder()
          .cartItem(item)                        // IMPORTANT: owning side for detail
          .itemSerialNo(t.getItemSerialNo())
          .imeiNo(t.getImeiNo())
          .batteryHealth(t.getBatteryHealth())
          .warranty(t.getWarranty())
          .boxFlag(t.getBoxFlag())
          .chargerFlag(t.getChargerFlag())
          .sealedFlag(t.getSealedFlag())
          .invoiceFlag(t.getInvoiceFlag())
          .acquisitionCost(t.getAcquisitionCost())
          .refurbishedCost(t.getRefurbishedCost())
          .sellingCost(req.getSellingCost())
          .ramRomSpecs(t.getRamRomSpecs())
          .colorSpecs(t.getColorSpecs())
          .comment(t.getComment())
          .productName(t.getProductName())
          .brand(t.getBrand())
          .build();

      // keep the inverse side in sync
      item.getDetails().add(detail);
      sellCart.getItems().add(item);
    }

    // cascade from Cart -> CartItem -> CartItemDetail will persist everything
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

  @Transactional
  public CartItemDTO patchCartItem(
      String userEmail,
      String cartType,
      Long cartItemId,
      CartItemPatchRequest req
  ) {
    Cart cart = cartRepo.findByUserEmailAndCartType(userEmail, cartType)
        .orElseThrow(() -> new EntityNotFoundException("Cart not found for user/type"));

    // Load item, ensure it belongs to this cart
    CartItem item = cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + cartItemId));

    if (!item.getCart().getId().equals(cart.getId())) {
      throw new IllegalArgumentException("Cart item does not belong to user's cart");
    }

    if (req.details() != null) {
      Map<Long, CartItemDetail> existingById = item.getDetails().stream()
          .filter(d -> d.getId() != null)
          .collect(Collectors.toMap(CartItemDetail::getId, Function.identity()));

      Set<Long> seen = new HashSet<>();

      for (CartItemDetailUpdateDto d : req.details()) {
        CartItemDetail entity;
        if (d.id() != null) {
          entity = existingById.get(d.id());
          if (entity == null) {
            throw new EntityNotFoundException("Detail not found: " + d.id());
          }
          seen.add(d.id());
        } else {
          entity = CartItemDetail.builder()
              .cartItem(item)
              .build();
          item.getDetails().add(entity);
        }

        if (d.acquisitionCost() != null) entity.setAcquisitionCost(d.acquisitionCost());
        if (d.refurbishedCost() != null) entity.setRefurbishedCost(d.refurbishedCost());
        if (d.ramRomSpecs() != null)    entity.setRamRomSpecs(d.ramRomSpecs());
        if (d.colorSpecs() != null)     entity.setColorSpecs(d.colorSpecs());
        if (d.sellingCost() != null)    entity.setSellingCost(d.sellingCost());
      }
    }

    cartItemRepository.save(item);

    List<CartItemDetailDTO> detailDtos = item.getDetails().stream()
        .map(d -> new CartItemDetailDTO(
            d.getId(), d.getItemSerialNo(), d.getImeiNo(), d.getBatteryHealth(), d.getWarranty(),
            d.getBoxFlag(), d.getChargerFlag(), d.getSealedFlag(), d.getInvoiceFlag(),
            d.getAcquisitionCost(), d.getRefurbishedCost(), d.getSellingCost(), d.getRamRomSpecs(), d.getColorSpecs(),
            d.getComment(), d.getProductName(), d.getBrand()
        ))
        .toList();

    return new CartItemDTO(item.getId(), item.getItemId(), item.getQuantity(), detailDtos);
  }

}

package com.erp.erp.application.ticket;

import com.erp.erp.application.dto.BillDto;
import com.erp.erp.application.dto.InvoiceDto;
import com.erp.erp.application.dto.TicketDto;
import com.erp.erp.application.dto.TicketStatusCount;
import com.erp.erp.application.dto.response.BillResponseDto;
import com.erp.erp.application.dto.response.InvoiceResponseDto;
import com.erp.erp.application.dto.response.TicketResponseDto;
import com.erp.erp.application.item.CartService;
import com.erp.erp.domain.enums.TicketStatus;
import com.erp.erp.domain.model.client.Store;
import com.erp.erp.domain.model.invoice.Invoice;
import com.erp.erp.domain.model.invoice.InvoiceRepository;
import com.erp.erp.domain.model.item.Cart;
import com.erp.erp.domain.model.item.CartItem;
import com.erp.erp.domain.model.item.CartItemDetail;
import com.erp.erp.domain.model.item.CartRepository;
import com.erp.erp.domain.model.payment.Payment;
import com.erp.erp.domain.model.ticket.SoldStatus;
import com.erp.erp.domain.model.ticket.SoldStatusRepository;
import com.erp.erp.domain.model.ticket.Ticket;
import com.erp.erp.domain.model.ticket.TicketLifecycle;
import com.erp.erp.domain.model.ticket.TicketRepository;
import com.erp.erp.domain.model.user.User;
import com.erp.erp.domain.model.user.UserRepository;
import com.erp.erp.infrastructure.utility.BillMapper;
import com.erp.erp.infrastructure.utility.DateTimeFormatterUtil;
import com.erp.erp.infrastructure.utility.InvoiceMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.EntityType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
public class TicketServiceImpl implements TicketService {

  private final TicketRepository ticketRepository;
  private static final Map<TicketStatus, Map<TicketStatus, Set<String>>> TRANSITION_ROLE_MAP;
  private static final String MANAGER = "ROLE_MANAGER";
  private static final String QC1_USER = "ROLE_QC1_USER";
  private static final String QC2_USER = "ROLE_QC2_USER";
  private static final String LISTED_USER = "ROLE_LISTED_USER";
  private final SoldStatusRepository soldStatusRepository;
  private final UserRepository userRepository;
  private final CartService cartService;
  private final InvoiceRepository invoiceRepository;
  private final CartRepository cartRepository;

  static {
    TRANSITION_ROLE_MAP = new EnumMap<>(TicketStatus.class);

    TRANSITION_ROLE_MAP.put(TicketStatus.QC, Map.of(
        TicketStatus.FACTORY, Set.of(QC1_USER, MANAGER),
        TicketStatus.LISTED, Set.of(QC1_USER, MANAGER),
        TicketStatus.SCRAPED, Set.of(QC1_USER, MANAGER))
    );
    TRANSITION_ROLE_MAP.put(TicketStatus.FACTORY, Map.of(
        TicketStatus.QC, Set.of(QC1_USER, MANAGER),
        TicketStatus.LISTED, Set.of(QC2_USER, MANAGER),
        TicketStatus.SCRAPED, Set.of(QC2_USER, MANAGER))
    );
    TRANSITION_ROLE_MAP.put(TicketStatus.LISTED, Map.of(
        TicketStatus.SCRAPED, Set.of(LISTED_USER, MANAGER),
        TicketStatus.QC, Set.of(LISTED_USER, MANAGER),
        TicketStatus.FACTORY, Set.of(LISTED_USER, MANAGER))
    );

  }

  public Long createTicket(TicketDto ticketDto, String email) {
    return createATicketForNewPurchase(ticketDto, email);
  }

  @Override
  public void updateTicketStatus(Long ticketId, TicketStatus newTicketStatus, String comment, BigDecimal cost) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
    TicketStatus current = ticket.getTicketStatus();

    Map<TicketStatus, Set<String>> allowedMap = TRANSITION_ROLE_MAP.getOrDefault(current, Map.of());
    if (!allowedMap.containsKey(newTicketStatus)) {
      throw new IllegalArgumentException(
          "Invalid status transition from " + current + " to " + newTicketStatus);
    }

    Set<String> requiredRoles = allowedMap.get(newTicketStatus);
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean hasRole = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(requiredRoles::contains);
    if (!hasRole) {
      throw new AccessDeniedException(
          "You need one of roles " + requiredRoles + " to make this transition");
    }
    TicketStatus oldTicketStatus = ticket.getTicketStatus();
    String newComment = null;
    if (comment != null) {
      String comments =
          "Ticket has been updated to status " + newTicketStatus + " on " + DateTimeFormatterUtil.toReadable(
              LocalDateTime.now()) +
              ".\nComment added by " + ticket.getUserEmail() + " : " + comment;
      newComment = ticket.getComment() + comments;
    }
    BigDecimal newRefurbishedCost;
    if (ticket.getRefurbishedCost() == null) {
      newRefurbishedCost = cost;
    } else {
      newRefurbishedCost = ticket.getRefurbishedCost().add(cost);
    }
    ticket.setComment(newComment);
    ticket.setRefurbishedCost(newRefurbishedCost);
    ticket.setTicketStatus(newTicketStatus);
    TicketLifecycle ticketLifecycle = TicketLifecycle.builder()
        .ticket(ticket)
        .userEmail(ticket.getUserEmail())
        .comment(comment)
        .statusChangeTime(Instant.now())
        .prevTicketStatus(oldTicketStatus)
        .newTicketStatus(newTicketStatus)
        .costAggregation(cost)
        .build();
    ticket.getLifecycles().add(ticketLifecycle);
    ticketRepository.save(ticket);
  }

//  public void createBillAndMoveToSold(Long ticketId, BillDto billDto) {
//    Ticket ticket = ticketRepository.findById(ticketId)
//        .orElseThrow(() -> new IllegalArgumentException("Ticket not found : " + ticketId));
//    if (ticket.getTicketStatus() == TicketStatus.SOLD) {
//      throw new IllegalArgumentException("Product already sold : " + ticketId);
//    } else if (ticket.getTicketStatus() != TicketStatus.LISTED) {
//      throw new IllegalArgumentException("Product need to be on Listed Status to be sold : " + ticketId);
//    } else if (ticket.getIsDeleted().equalsIgnoreCase("Y")) {
//      throw new IllegalArgumentException("This ticket is deleted : " + ticketId);
//    } else {
//      ticket.setTicketStatus(TicketStatus.SOLD);
//      SoldStatus soldStatus = SoldStatus.builder()
//          .ticketId(ticketId)
//          .clientId(ticket.getClientId())
//          .phoneNumber(billDto.phoneNumber())
//          .customerName(billDto.customerName())
//          .phoneNumber(billDto.phoneNumber())
//          .gstId(billDto.gstId())
//          .onlineTrxId(billDto.onlineTrxId())
//          .modeOfPayment(billDto.modeOfPayment())
//          .placeOfSale(billDto.placeOfSale())
//          .profit(billDto.profit())
//          .billNumber(UUID.randomUUID().toString())
//          .billDate(LocalDate.now())
//          .isDeleted("N")
//          .build();
//      soldStatusRepository.save(soldStatus);
//    }
//  }

  @Override
  public List<Ticket> searchQC1Data(String email) {
    return ticketRepository.findByTicketStatusAndUserEmail(TicketStatus.QC, email);
  }

  @Override
  public List<TicketResponseDto> searchTickets(TicketStatus status, String email) {
    Optional<User> user = userRepository.findByUserEmail(email);
    if (user.isEmpty()) {
      throw new EntityNotFoundException("No user found with this user email");
    }
    Set<Long> storeIds = user
        .get().getStores()
        .stream()
        .map(Store::getId)
        .collect(Collectors.toSet());
    List<Ticket> ticketPage = ticketRepository.findByTicketStatusAndStore_IdIn(status, storeIds);
    return ticketPage.stream().map(this::mapToTicketResponseDto
    ).collect(Collectors.toList());
  }

  private TicketResponseDto mapToTicketResponseDto(Ticket ticket) {
    return TicketResponseDto.builder()
        .ticketId(ticket.getTicketId())
        .clientId(ticket.getClientId())
        .ticketStatus(ticket.getTicketStatus())
        .phoneNumber(ticket.getPhoneNumber())
        .customerName(ticket.getCustomerName())
        .productPurchaseType(ticket.getProductPurchaseType())
        .acquisitionCost(ticket.getAcquisitionCost())
        .refurbishedCost(ticket.getRefurbishedCost())
        .documentType(ticket.getType())
        .customerDocumentId(ticket.getCustomerDocumentId())
        .itemId(ticket.getItemId())
        .brand(ticket.getBrand())
        .userEmail(ticket.getUserEmail())
        .itemSerialNo(ticket.getItemSerialNo())
        .imeiNo(ticket.getImeiNo())
        .batteryHealth(ticket.getBatteryHealth())
        .warranty(ticket.getWarranty())
        .boxFlag(ticket.getBoxFlag())
        .chargerFlag(ticket.getChargerFlag())
        .sealedFlag(ticket.getSealedFlag())
        .invoiceFlag(ticket.getInvoiceFlag())
        .ramRomSpecs(ticket.getRamRomSpecs())
        .colorSpecs(ticket.getColorSpecs())
        .comment(ticket.getComment())
        .productName(ticket.getProductName())
        .isDeleted(ticket.getIsDeleted())
        .storeId(ticket.getStore() != null ? ticket.getStore().getId() : null)
        .storeName(ticket.getStore() != null ? ticket.getStore().getName() : null)
        .invoiceDto(ticket.getInvoice() != null
            ? InvoiceMapper.toDto(ticket.getInvoice())
            : null)
        .billResponseDto(ticket.getBill() != null
            ? BillMapper.toDto(ticket.getBill())
            : null)
        .build();
  }

  @Override
  public List<Ticket> searchTicketsByUserName(String email) {
    return ticketRepository.findByUserEmail(email);
  }

  private Long createATicketForNewPurchase(TicketDto ticketDto, String email) {
    Optional<User> user = userRepository.findByUserEmail(email);
    if (user.isEmpty()) {
      throw new UsernameNotFoundException("No user found for user " + email);
    }
    TicketStatus newStatus;
    if (ticketDto.sealedFlag().equalsIgnoreCase("Y")) {
      newStatus = TicketStatus.LISTED;
    } else {
      newStatus = TicketStatus.QC;
    }
    UUID invoiceUUID = UUID.randomUUID();
    String comments = null;
    if (ticketDto.comments() != null) {
      comments =
          "Ticket has created with status " + newStatus + " on " + DateTimeFormatterUtil.toReadable(
              LocalDateTime.now()) +
              ".\nComment added by " + email + " : " + ticketDto.comments();
    }
    Ticket newTicket = Ticket.builder()
        .userEmail(email)
        .clientId(user.get().getClientId())
        .ticketStatus(newStatus)
        .phoneNumber(ticketDto.phoneNumber())
        .customerName(ticketDto.customerName())
        .productPurchaseType(ticketDto.productPurchaseType())
//        .modeOfPayment(ticketDto.modeOfPayment())
        .type(ticketDto.documentType())
        .customerDocumentId(ticketDto.customerDocumentId())
        .itemId(ticketDto.itemId())
        .acquisitionCost(ticketDto.acquisitionCost())
        .refurbishedCost(ticketDto.refurbishedCost())
        .isDeleted("N")
        .itemSerialNo(ticketDto.itemSerialNo())
        .imeiNo(ticketDto.imeiNo())
        .batteryHealth(ticketDto.batteryHealth())
        .warranty(ticketDto.warranty())
        .boxFlag(ticketDto.boxFlag())
        .chargerFlag(ticketDto.chargerFlag())
        .sealedFlag(ticketDto.sealedFlag())
        .invoiceFlag(ticketDto.invoiceFlag())
        .acquisitionCost(ticketDto.acquisitionCost())
        .refurbishedCost(ticketDto.refurbishedCost())
        .ramRomSpecs(ticketDto.ramRomSpecs())
        .colorSpecs(ticketDto.ColorSpecs())
        .comment(comments)
        .productName(ticketDto.productName())
        .brand(ticketDto.brand())
        .build();
    TicketLifecycle ticketLifecycle = TicketLifecycle.builder()
        .ticket(newTicket)
        .userEmail(email)
        .comment(comments)
        .statusChangeTime(Instant.now())
        .prevTicketStatus(null)
        .newTicketStatus(newStatus)
        .build();
    newTicket.getLifecycles().add(ticketLifecycle);
    ticketRepository.save(newTicket);
    return newTicket.getTicketId();
  }

  @Override
  public List<TicketResponseDto> findTicketBySpecification(
      Map<String, String> allParams,
      String username
  ) {
    Optional<User> user = userRepository.findByUserEmail(username);
    if (user.isEmpty()) {
      throw new EntityNotFoundException("No user found with this user email");
    }
    Set<Long> storeIds = user
        .get().getStores()
        .stream()
        .map(Store::getId)
        .collect(Collectors.toSet());
    Specification<Ticket> spec = buildSpecification(allParams, storeIds);
    List<Ticket> ticketPage = ticketRepository.findAll(spec);
    return ticketPage.stream().map(this::mapToTicketResponseDto
    ).collect(Collectors.toList());
  }

  @Override
  public List<TicketResponseDto> findInventoryTicketBySpecification(
      Map<String, String> allParams,
      String username
  ) {
    Optional<User> user = userRepository.findByUserEmail(username);
    if (user.isEmpty()) {
      throw new EntityNotFoundException("No user found with this user email");
    }
    Set<Long> storeIds = user
        .get().getStores()
        .stream()
        .map(Store::getId)
        .collect(Collectors.toSet());
    Specification<Ticket> baseSpec = buildSpecification(allParams, storeIds);
    Specification<Ticket> notSoldSpec =
        (root, cq, cb) -> cb.notEqual(root.get("ticketStatus"), TicketStatus.SOLD);
    Specification<Ticket> combined = Specification.where(baseSpec)
        .and(notSoldSpec);
    List<Ticket> ticketList = ticketRepository.findAll(combined);
    return ticketList.stream().map(this::mapToTicketResponseDto
    ).collect(Collectors.toList());
  }

  private Specification<Ticket> buildSpecification(
      Map<String, String> allParams,
      Set<Long> storeIds
  ) {
    Set<String> CONTAINS_FIELDS = Set.of(
        "ramRomSpecs",
        "productName",
        "brand",
        "imeiNo"
    );

    String fromDateStr = allParams.remove("invoiceDateFrom");
    String toDateStr = allParams.remove("invoiceDateTo");
    String minCostStr = allParams.remove("costMin");
    String maxCostStr = allParams.remove("costMax");
    String invoiceNumber = allParams.remove("invoiceNumber");

    return (root, cq, cb) -> {
      List<Predicate> preds = new ArrayList<>();

      preds.add(root
          .get("store")
          .get("id")
          .in(storeIds)
      );
      Join<Object, Object> invoiceJoin = root.join("invoice", JoinType.LEFT);

      if (fromDateStr != null || toDateStr != null) {
        Path<LocalDate> datePath = invoiceJoin.get("invoiceDate");

        if (fromDateStr != null && toDateStr != null) {
          LocalDate from = LocalDate.parse(fromDateStr);
          LocalDate to = LocalDate.parse(toDateStr);
          preds.add(cb.between(datePath, from, to));
        } else if (fromDateStr != null) {
          LocalDate from = LocalDate.parse(fromDateStr);
          preds.add(cb.greaterThanOrEqualTo(datePath, from));
        } else {
          LocalDate to = LocalDate.parse(toDateStr);
          preds.add(cb.lessThanOrEqualTo(datePath, to));
        }
      }


      if (invoiceNumber != null && !invoiceNumber.isBlank()) {
        preds.add(cb.like(cb.lower(invoiceJoin.get("invoiceNumber")), "%" + invoiceNumber.toLowerCase() + "%"));
      }

      if (minCostStr != null || maxCostStr != null) {
        Expression<BigDecimal> totalCost =
            cb.sum(root.get("acquisitionCost"), root.get("refurbishedCost"));
        if (minCostStr != null && maxCostStr != null) {
          BigDecimal min = new BigDecimal(minCostStr);
          BigDecimal max = new BigDecimal(maxCostStr);
          preds.add(cb.between(totalCost, min, max));
        } else if (minCostStr != null) {
          BigDecimal min = new BigDecimal(minCostStr);
          preds.add(cb.greaterThanOrEqualTo(totalCost, min));
        } else {
          BigDecimal max = new BigDecimal(maxCostStr);
          preds.add(cb.lessThanOrEqualTo(totalCost, max));
        }
      }

      EntityType<Ticket> meta = root.getModel();
      for (var entry : allParams.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (value == null || value.isBlank()) {
          continue;
        }

        Class<?> javaType = meta.getAttribute(key).getJavaType();

        if (String.class.equals(javaType)) {
          if (CONTAINS_FIELDS.contains(key)) {
            preds.add(cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%"));
          } else {
            preds.add(cb.equal(root.get(key), value));
          }
        } else if (Number.class.isAssignableFrom(javaType)) {
          Long longVal = Long.valueOf(value);
          preds.add(cb.equal(root.get(key), longVal));
        } else if (Boolean.class.equals(javaType) || boolean.class.equals(javaType)) {
          Boolean boolVal = Boolean.valueOf(value);
          preds.add(cb.equal(root.get(key), boolVal));
        } else if (LocalDate.class.equals(javaType)) {
          LocalDate dateVal = LocalDate.parse(value);
          preds.add(cb.equal(root.get(key), dateVal));
        } else {
          preds.add(cb.like(root.get(key).as(String.class), "%" + value + "%"));
        }
      }

      return cb.and(preds.toArray(new Predicate[0]));
    };
  }


//  @Override
//  public BillDto checkBill(Long ticketId) {
//    Optional<SoldStatus> bill = soldStatusRepository.findByTicketId(ticketId);
//    if (bill.isEmpty()) {
//      throw new IllegalArgumentException("No Bill found.");
//    }
//    return BillDto.builder()
//        .clientId(bill.get().getClientId())
//        .customerName(bill.get().getCustomerName())
//        .phoneNumber(bill.get().getPhoneNumber())
//        .modeOfPayment(bill.get().getModeOfPayment())
//        .gstId(bill.get().getGstId())
//        .modeOfPayment(bill.get().getModeOfPayment())
//        .onlineTrxId(bill.get().getOnlineTrxId())
//        .placeOfSale(bill.get().getPlaceOfSale())
//        .billNumber(bill.get().getBillNumber())
//        .billDate(bill.get().getBillDate())
//        .profit(bill.get().getProfit())
//        .build();
//  }

  @Override
  public TicketResponseDto checkTicket(Long ticketId) {
    Optional<Ticket> ticket = ticketRepository.findByTicketId(ticketId);
    if (ticket.isEmpty()) {
      throw new IllegalArgumentException("No ticket found.");
    }
    return mapToTicketResponseDto(ticket.get());
  }

  @Override
  @Transactional
  public InvoiceResponseDto checkoutForBuyCart(String userEmail, InvoiceDto invoiceDto) {
    Optional<User> user = userRepository.findByUserEmail(userEmail);
    if (user.isEmpty()) {
      throw new UsernameNotFoundException("No user found for user " + userEmail);
    }
    Cart cart = cartService.getOrCreateBuyCart(userEmail);
    List<Ticket> tickets = new ArrayList<>();
    BigDecimal totalAcqCost = BigDecimal.ZERO;
    for (CartItem item : cart.getItems()) {
      for (CartItemDetail detail : item.getDetails()) {
        tickets.add(createTicketForNewPurchase(detail, item, invoiceDto, user.get()));
        totalAcqCost = totalAcqCost.add(detail.getAcquisitionCost());
      }
    }
    cartService.clearCart(cart);
    Invoice invoice = createInvoice(user.get(), invoiceDto, tickets, totalAcqCost);
    return InvoiceMapper.mapToInvoiceResponseDto(invoice);
  }

  @Transactional
  public Invoice createInvoice(User user, InvoiceDto dto, List<Ticket> tickets, BigDecimal totalAcqCost) {
    Optional<Store> matchingStore = user.getStores().stream()
        .filter(s -> s.getId().equals(dto.storeId()))
        .findFirst();
    if (matchingStore.isEmpty()) {
      throw new IllegalArgumentException("No store found with your ID for " + user.getUserEmail());
    }
    UUID invoiceUUID = UUID.randomUUID();
    List<Payment> payments = dto.payments().stream().map(paymentDto -> Payment.builder()
        .modeOfPayment(paymentDto.getModeOfPayment())
        .transactionId(paymentDto.getTransactionId())
        .amount(paymentDto.getAmount())
        .paidAt(paymentDto.getPaidAt() != null ? paymentDto.getPaidAt() : LocalDate.now())
        .build()).toList();

    BigDecimal totalPaid = payments.stream()
        .map(Payment::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    System.out.println("total :" + totalPaid + " " + totalAcqCost);
    if (totalPaid.compareTo(totalAcqCost) < 0) {
      throw new IllegalArgumentException("Please pay full cart amount " + totalAcqCost + " before proceeding.");
    }

    System.out.println("storeId : " + dto.storeId());
    Invoice invoice = Invoice.builder()
        .phoneNumber(dto.phoneNumber())
        .customerName(dto.customerName())
        .type(dto.documentType())
        .customerDocumentId(dto.customerDocumentId())
        .storeId(dto.storeId())
        .invoiceNumber(invoiceUUID.toString())
        .invoiceDate(LocalDate.now())
        .totalAmount(totalPaid)
        .gstNumber(dto.gstNumber())
        .gstId(dto.gstId())
        .build();
    payments.forEach(p -> p.setInvoice(invoice));
    tickets.forEach(t -> t.setInvoice(invoice));
    invoice.setPayments(payments);
    invoice.setTickets(tickets);
    return invoiceRepository.save(invoice);
  }

  @Transactional
  public BigDecimal calculateCartCost(String userEmail, String cartType) {
    Cart cart = cartRepository.findCartWithItems(userEmail, cartType)
        .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

    return getTotalAcquisitionCost(cart);
  }

  private BigDecimal getTotalAcquisitionCost(Cart cart) {
    System.out.println("Cart : " + cart.getItems());
    if (cart == null || cart.getItems() == null) {
      return BigDecimal.ZERO;
    }

    return cart.getItems().stream()
        .filter(item -> item.getDetails() != null)
        .flatMap(item -> item.getDetails().stream())
        .map(CartItemDetail::getAcquisitionCost)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  @Transactional
  public BillResponseDto checkoutSellCart(String userEmail, BillDto billDto) {
    Optional<User> user = userRepository.findByUserEmail(userEmail);
    if (user.isEmpty()) {
      throw new UsernameNotFoundException("No user found for user " + userEmail);
    }
    Optional<Store> matchingStore = user.get().getStores().stream()
        .filter(s -> s.getId().equals(billDto.storeId()))
        .findFirst();
    if (matchingStore.isEmpty()) {
      throw new IllegalArgumentException("No store found with your ID for " + user.get().getUserEmail());
    }
    Cart sellCart = cartService.getOrCreateSellCart(userEmail);

    List<Long> ticketIds = sellCart.getItems().stream()
        .map(CartItem::getItemId)
        .collect(Collectors.toList());

    List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
    for (Ticket ticket : tickets) {
      if (ticket.getTicketStatus() == TicketStatus.SOLD) {
        throw new IllegalArgumentException("Product already sold : " + ticket.getTicketId());
      } else if (ticket.getTicketStatus() != TicketStatus.LISTED) {
        throw new IllegalArgumentException("Product need to be on Listed Status to be sold : " + ticket.getTicketId());
      } else if (ticket.getIsDeleted().equalsIgnoreCase("Y")) {
        throw new IllegalArgumentException("This ticket is deleted : " + ticket.getTicketId());
      } else {
        ticket.setTicketStatus(TicketStatus.SOLD);
      }
    }
    System.out.println("storeId sell : " + billDto.storeId());
    SoldStatus soldStatus = SoldStatus.builder()
        .clientId(user.get().getClientId())
        .phoneNumber(billDto.phoneNumber())
        .customerName(billDto.customerName())
        .type(billDto.documentType())
        .customerDocumentId(billDto.customerDocumentId())
        .storeId(billDto.storeId())
        .gstId(billDto.gstId())
        .onlineTrxId(billDto.onlineTrxId())
        .placeOfSale(billDto.placeOfSale())
        .profit(billDto.profit())
        .billNumber(UUID.randomUUID().toString())
        .billDate(LocalDate.now())
        .gstNumber(billDto.gstNumber())
        .isDeleted("N")
        .build();
    List<Payment> payments = billDto.payments().stream().map(paymentDto -> Payment.builder()
        .modeOfPayment(paymentDto.getModeOfPayment())
        .transactionId(paymentDto.getTransactionId())
        .amount(paymentDto.getAmount())
        .paidAt(paymentDto.getPaidAt() != null ? paymentDto.getPaidAt() : LocalDate.now())
        .bill(soldStatus)
        .build()).toList();
    payments.forEach(p -> p.setBill(soldStatus));
    tickets.forEach(t -> t.setBill(soldStatus));
    soldStatus.setPayments(payments);
    soldStatus.setTickets(tickets);
    soldStatusRepository.save(soldStatus);
    ticketRepository.saveAll(tickets);
    cartService.clearCart(sellCart);
    return BillMapper.toDto(soldStatus);
  }


  private Ticket createTicketForNewPurchase(CartItemDetail cart, CartItem item, InvoiceDto invoiceDto, User user) {
    TicketStatus newStatus;
    if (cart.getSealedFlag().equalsIgnoreCase("Y")) {
      newStatus = TicketStatus.LISTED;
    } else {
      newStatus = TicketStatus.QC;
    }
    String comments = null;
    if (cart.getComment() != null) {
      comments =
          "Ticket has created with status " + newStatus + " on " + DateTimeFormatterUtil.toReadable(
              LocalDateTime.now()) +
              ".\nComment added by " + user.getUserEmail() + " : " + cart.getComment();
    }
    Optional<Store> matchingStore = user.getStores().stream()
        .filter(s -> s.getId().equals(invoiceDto.storeId()))
        .findFirst();
    if (matchingStore.isEmpty()) {
      throw new IllegalArgumentException("No store found with your ID for " + user.getUserEmail());
    }
    Ticket newTicket = Ticket.builder()
        .store(matchingStore.get())
        .userEmail(user.getUserEmail())
        .clientId(user.getClientId())
        .ticketStatus(newStatus)
        .phoneNumber(invoiceDto.phoneNumber())
        .customerName(invoiceDto.customerName())
//        .gstNumber(invoiceDto.gstNumber())
//        .gstId(invoiceDto.gstId())
        .productPurchaseType("MOBILE")
//        .modeOfPayment(invoiceDto.modeOfPayment())
        .type(invoiceDto.documentType())
        .customerDocumentId(invoiceDto.customerDocumentId())
        .itemId(item.getItemId())
        .acquisitionCost(cart.getAcquisitionCost())
        .refurbishedCost(cart.getRefurbishedCost())
        .isDeleted("N")
        .itemSerialNo(cart.getImeiNo())
        .imeiNo(cart.getImeiNo())
        .batteryHealth(cart.getBatteryHealth())
        .warranty(cart.getWarranty())
        .boxFlag(cart.getBoxFlag())
        .chargerFlag(cart.getChargerFlag())
        .sealedFlag(cart.getSealedFlag())
        .invoiceFlag(cart.getInvoiceFlag())
        .ramRomSpecs(cart.getRamRomSpecs())
        .colorSpecs(cart.getColorSpecs())
        .comment(comments)
        .productName(cart.getProductName())
        .brand(cart.getBrand())
        .build();

    TicketLifecycle ticketLifecycle = TicketLifecycle.builder()
        .ticket(newTicket)
        .userEmail(user.getUserEmail())
        .comment(comments)
        .statusChangeTime(Instant.now())
        .prevTicketStatus(null)
        .newTicketStatus(newStatus)
        .build();
    newTicket.getLifecycles().add(ticketLifecycle);
    return newTicket;
  }

  @Override
  public List<TicketStatusCount> getTicketCountsByStatus(String username) {
    Optional<User> user = userRepository.findByUserEmail(username);
    if (user.isEmpty()) {
      throw new EntityNotFoundException("No user with this user name");
    }
    return ticketRepository.countTicketsByStatus(user.get().getStores());
  }


}

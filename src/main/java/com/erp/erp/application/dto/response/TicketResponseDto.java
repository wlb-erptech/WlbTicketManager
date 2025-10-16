package com.erp.erp.application.dto.response;

import com.erp.erp.domain.enums.CustomerIdType;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.enums.TicketStatus;
import com.erp.erp.domain.model.ticket.Ticket;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketResponseDto {

  private Long ticketId;
  private Long clientId;
  private TicketStatus ticketStatus;
  private String phoneNumber;
  private String customerName;
  private String productPurchaseType;
  private PaymentMode modeOfPayment;
  private BigDecimal acquisitionCost;
  private BigDecimal refurbishedCost;
  private CustomerIdType documentType;
  private String customerDocumentId;
  private Long itemId;
  private String brand;
  private String userEmail;
  private String itemSerialNo;
  private String imeiNo;
  private String batteryHealth;
  private String warranty;
  private String boxFlag;
  private String chargerFlag;
  private String sealedFlag;
  private String invoiceFlag;
  private String ramRomSpecs;
  private String colorSpecs;
  private String comment;
  private String productName;
  private String isDeleted;
  private Long storeId;
  private String storeName;
  private InvoiceResponseDto invoiceDto;
  private BillResponseDto billResponseDto;

  public TicketResponseDto mapToDto(Ticket ticket) {
    return TicketResponseDto.builder()
        .ticketId(ticket.getTicketId())
        .clientId(ticket.getClientId())
        .ticketStatus(ticket.getTicketStatus())
//        .invoiceNumber(ticket.getInvoiceNumber())
//        .invoiceDate(ticket.getInvoiceDate())
//        .phoneNumber(ticket.getPhoneNumber())
//        .customerName(ticket.getCustomerName())
//        .gstNumber(ticket.getGstNumber())
//        .gstId(ticket.getGstId())
        .productPurchaseType(ticket.getProductPurchaseType())
//        .modeOfPayment(ticket.getModeOfPayment())
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
        .build();
  }

}

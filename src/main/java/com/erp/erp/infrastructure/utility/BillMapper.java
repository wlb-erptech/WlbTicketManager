package com.erp.erp.infrastructure.utility;

import com.erp.erp.application.dto.InvoiceProductDto;
import com.erp.erp.application.dto.PaymentDto;
import com.erp.erp.application.dto.response.BillResponseDto;
import com.erp.erp.domain.enums.PaymentMode;
import com.erp.erp.domain.model.ticket.SoldStatus;
import com.erp.erp.domain.model.ticket.Ticket;
import com.erp.erp.domain.model.payment.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class BillMapper {

  public static BillResponseDto toDto(SoldStatus bill) {
    BigDecimal remainingCredit = bill.getPayments().stream()
        .filter(payment -> payment.getModeOfPayment() == PaymentMode.CREDIT)
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return BillResponseDto.builder()
        .billId(bill.getSoldTableId())
        .billNumber(bill.getBillNumber())
        .billDate(bill.getBillDate())
        .clientId(bill.getClientId())
        .remainingCredit(remainingCredit)
        .customerName(bill.getCustomerName())
        .phoneNumber(bill.getPhoneNumber())
        .gstNumber(bill.getGstNumber())
        .gstId(bill.getGstId())
        .onlineTrxId(bill.getOnlineTrxId())
        .placeOfSale(bill.getPlaceOfSale())
        .profit(bill.getProfit())
        .payments(mapPayments(bill.getPayments()))
        .products(mapProducts(bill.getTickets()))
        .build();
  }

  private static List<PaymentDto> mapPayments(List<Payment> payments) {
    return payments.stream().map(p -> PaymentDto.builder()
        .modeOfPayment(p.getModeOfPayment())
        .amount(p.getAmount())
        .transactionId(p.getTransactionId())
        .paidAt(p.getPaidAt())
        .build()).collect(Collectors.toList());
  }

  private static List<InvoiceProductDto> mapProducts(List<Ticket> tickets) {
    return tickets.stream().map(t -> InvoiceProductDto.builder()
        .ticketId(t.getTicketId())
        .itemId(t.getItemId())
        .productName(t.getProductName())
        .brand(t.getBrand())
        .ramRomSpecs(t.getRamRomSpecs())
        .colorSpecs(t.getColorSpecs())
        .acquisitionCost(t.getAcquisitionCost())
        .refurbishedCost(t.getRefurbishedCost())
        .imeiNo(t.getImeiNo())
        .serialNo(t.getItemSerialNo())
        .boxFlag(t.getBoxFlag())
        .chargerFlag(t.getChargerFlag())
        .sealedFlag(t.getSealedFlag())
        .warranty(t.getWarranty())
        .build()).collect(Collectors.toList());
  }
}

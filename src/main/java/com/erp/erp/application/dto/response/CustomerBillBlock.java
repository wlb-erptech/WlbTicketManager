package com.erp.erp.application.dto.response;

public record CustomerBillBlock(
    String customerName,
    String phoneNumber,
    String gstNumber,   // optional
    String gstId,       // optional
    java.util.List<BillBlock> bills
) {}
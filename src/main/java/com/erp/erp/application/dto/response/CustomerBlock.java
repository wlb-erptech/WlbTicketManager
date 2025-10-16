package com.erp.erp.application.dto.response;

public record CustomerBlock(
    String customerName, String phoneNumber, Long customerAadharId,
    java.util.List<InvoiceBlock> invoices
) {}
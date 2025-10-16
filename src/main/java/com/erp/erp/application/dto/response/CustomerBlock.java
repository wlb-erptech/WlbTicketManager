package com.erp.erp.application.dto.response;

public record CustomerBlock(
    String customerName, String phoneNumber, String customerDocumentId,
    java.util.List<InvoiceBlock> invoices
) {}
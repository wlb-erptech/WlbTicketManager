package com.erp.erp.application.dto.response;

public record StoreCustomerBillHistoryDto(
    int page, int size, long totalCustomers,
    java.util.List<CustomerBillBlock> customers
) {}
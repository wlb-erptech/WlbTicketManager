package com.erp.erp.application.dto.response;

public record StoreCustomerHistoryDto(
    int page, int size, long totalCustomers,
    java.util.List<CustomerBlock> customers
) {}

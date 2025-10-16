package com.erp.erp.application.controller;

import com.erp.erp.application.customer.StoreCustomerBillHistoryService;
import com.erp.erp.application.customer.StoreCustomerHistoryService;
import com.erp.erp.application.dto.response.StoreCustomerBillHistoryDto;
import com.erp.erp.application.dto.response.StoreCustomerHistoryDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("isAuthenticated()")
@lombok.RequiredArgsConstructor
public class CustomerController {
  private final StoreCustomerHistoryService invoiceService;
  private final StoreCustomerBillHistoryService billService;

  @GetMapping("/all-invoices")
  public StoreCustomerHistoryDto invoiceHistory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @AuthenticationPrincipal(expression = "username") String username) {
    return invoiceService.getHistory(username, page, size);
  }

  @GetMapping("/all-bills")
  public StoreCustomerBillHistoryDto billHistory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @AuthenticationPrincipal(expression = "username") String username) {
    return billService.getBillHistory(username, page, size);
  }

}

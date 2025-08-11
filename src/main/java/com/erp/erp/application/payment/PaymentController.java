package com.erp.erp.application.payment;

import com.erp.erp.application.dto.response.BillResponseDto;
import com.erp.erp.application.dto.response.InvoiceResponseDto;
import com.erp.erp.application.dto.response.PaymentRequestDto;
import com.erp.erp.application.dto.response.PaymentResponseDto;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
public class PaymentController {
  private final PaymentService svc;
  public PaymentController(PaymentService svc) { this.svc = svc; }

  @GetMapping("/invoice/credit")
  @PreAuthorize("hasAnyRole('USER','ADMIN','MANAGER')")
  public List<InvoiceResponseDto> listInvoiceCreditPayments() {
    return svc.getInvoiceCreditPayments();
  }

  @GetMapping("/bill/credit")
  @PreAuthorize("hasAnyRole('USER','ADMIN','MANAGER')")
  public List<BillResponseDto> listBillCreditPayments() {
    return svc.getBillCreditPayments();
  }

  @PostMapping("/credit/invoice/repayment")
  @PreAuthorize("hasAnyRole('USER','ADMIN','MANAGER')")
  public InvoiceResponseDto repayCreditForInvoice(@RequestBody PaymentRequestDto paymentRequestDto) {
    return svc.addPaymentForInvoice(paymentRequestDto);
  }

  @PostMapping("/credit/bill/repayment")
  @PreAuthorize("hasAnyRole('USER','ADMIN','MANAGER')")
  public BillResponseDto repayCreditForBill(@RequestBody PaymentRequestDto paymentRequestDto) {
    return svc.addPaymentForBill(paymentRequestDto);
  }




}

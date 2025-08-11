package com.erp.erp.domain.model.payment;

import com.erp.erp.application.dto.response.PaymentResponseDto;
import com.erp.erp.domain.enums.PaymentMode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  @Query("""
      SELECT new com.erp.erp.application.dto.response.PaymentResponseDto(
        'BUY',
        p.id,
        sum(p.amount),
        p.paidAt,
        t.customerName,
        i.id,
        t.ticketId
      )
      FROM Payment p
      JOIN p.invoice i
      JOIN i.tickets t
      WHERE p.modeOfPayment = :mode
      group by i.id
    """)
  List<PaymentResponseDto> findByModeWithInvoiceAndTicket(
      @Param("mode") PaymentMode mode
  );

  @Query("""
      SELECT new com.erp.erp.application.dto.response.PaymentResponseDto(
        'SELL',
        p.id,
        sum(p.amount),
        p.paidAt,
        b.customerName,
        b.soldTableId,
        t.ticketId
      )
      FROM Payment p
      JOIN p.bill b
      JOIN b.tickets t
      WHERE p.modeOfPayment = :mode
      group by b.soldTableId
    """)
  List<PaymentResponseDto> findByModeWithBillAndTicket(
      @Param("mode") PaymentMode mode
  );

}
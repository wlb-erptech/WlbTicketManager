package com.erp.erp.domain.model.invoice;

import com.erp.erp.domain.model.ticket.SoldStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

  @Query("""
        SELECT DISTINCT i
        FROM Invoice i
        JOIN i.payments p
        WHERE p.modeOfPayment = com.erp.erp.domain.enums.PaymentMode.CREDIT
      """)
  List<Invoice> findAllWithInvoiceCredits();

}
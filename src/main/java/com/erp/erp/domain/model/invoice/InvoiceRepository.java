package com.erp.erp.domain.model.invoice;

import com.erp.erp.domain.model.ticket.SoldStatus;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

  @Query("""
        SELECT DISTINCT i
        FROM Invoice i
        JOIN i.payments p
        WHERE p.modeOfPayment = com.erp.erp.domain.enums.PaymentMode.CREDIT
        and i.storeId in :storeIds
      """)
  List<Invoice> findAllWithInvoiceCredits(@Param("storeIds") Set<Long> storeId);

  @Query("""
    select i from Invoice i
    where i.storeId in :storeIds
    order by i.invoiceDate desc, i.id desc
  """)
  java.util.List<Invoice> findByStore(@Param("storeIds") Set<Long> storeId);

}
package com.erp.erp.domain.model.ticket;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SoldStatusRepository extends JpaRepository<SoldStatus, Long> {
    Optional<SoldStatus> findBySoldTableId(Long soldTableId);

    @Query("""
        SELECT DISTINCT b
        FROM SoldStatus b
        JOIN b.payments p
        WHERE p.modeOfPayment = com.erp.erp.domain.enums.PaymentMode.CREDIT
      """)
    List<SoldStatus> findAllWithBillCredits();

}

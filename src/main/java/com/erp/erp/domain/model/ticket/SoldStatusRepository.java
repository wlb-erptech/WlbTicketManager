package com.erp.erp.domain.model.ticket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SoldStatusRepository extends JpaRepository<SoldStatus, Long> {
    Optional<SoldStatus> findBySoldTableId(Long soldTableId);

    @Query("""
        SELECT DISTINCT b
        FROM SoldStatus b
        JOIN b.payments p
        WHERE p.modeOfPayment = com.erp.erp.domain.enums.PaymentMode.CREDIT
        and b.storeId in :storeIds
      """)
    List<SoldStatus> findAllWithBillCredits(@Param("storeIds") Set<Long> storeIds);

    @Query("""
    select s from SoldStatus s
    where s.storeId in :storeIds
    order by s.billDate desc, s.soldTableId desc
  """)
    List<SoldStatus> findByStores(Collection<Long> storeIds);

}

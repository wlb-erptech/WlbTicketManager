package com.erp.erp.domain.model.ticket;

import com.erp.erp.application.dto.TicketStatusCount;
import com.erp.erp.domain.enums.TicketStatus;
import com.erp.erp.domain.model.client.Store;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

  Optional<Ticket> findByTicketId(Long ticketId);

  List<Ticket> findByTicketStatusAndUserEmail(TicketStatus status, String userEmail);

  List<Ticket> findByUserEmail(String userEmail);

  @Query("""
       SELECT t.ticketStatus   AS status,
              COUNT(t)          AS count
         FROM Ticket t
        WHERE t.isDeleted <> 'Y'
          AND t.store IN :stores
        GROUP BY t.ticketStatus
       """)
  List<TicketStatusCount> countTicketsByStatus(@Param("stores") Collection<Store> stores);

  List<Ticket> findByTicketStatusAndStore_IdIn(
      TicketStatus status,
      Collection<Long> storeIds
  );

  Page<Ticket> findByStoreIn(Collection<Store> stores, Pageable pg);

  List<Ticket> findByInvoice_Id(Long invoiceId);

  List<Ticket> findByBill_SoldTableId(Long billId);


}

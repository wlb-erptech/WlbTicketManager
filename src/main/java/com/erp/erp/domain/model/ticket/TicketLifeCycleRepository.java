package com.erp.erp.domain.model.ticket;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketLifeCycleRepository extends JpaRepository<TicketLifecycle, Long> {
  Optional<TicketLifecycle> findByTicketLcId(Long ticketLcId);

  //AND COALESCE(tl.isDeleted, 'N') <> 'Y'
  @Query("""
    SELECT tl
    FROM TicketLifecycle tl
    WHERE tl.ticket.ticketId = :ticketId
      AND (:fromTs IS NULL OR tl.statusChangeTime >= :fromTs)
      AND (:toTs   IS NULL OR tl.statusChangeTime <  :toTs)
    ORDER BY tl.statusChangeTime DESC
""")
  List<TicketLifecycle> findHistoryList(
      @Param("ticketId") Long ticketId,
      @Param("fromTs") Instant fromTs,
      @Param("toTs") Instant toTs);

  Page<TicketLifecycle> findByTicket_TicketIdAndIsDeletedNotOrderByStatusChangeTimeDesc(
      Long ticketId, String isDeleted, Pageable pageable
  );
}

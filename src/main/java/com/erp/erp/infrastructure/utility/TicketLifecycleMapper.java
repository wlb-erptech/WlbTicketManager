package com.erp.erp.infrastructure.utility;

import com.erp.erp.application.dto.response.TicketLifecycleDto;
import com.erp.erp.domain.model.ticket.TicketLifecycle;

public final class TicketLifecycleMapper {
    private TicketLifecycleMapper() {}

    public static TicketLifecycleDto toDto(TicketLifecycle e) {
        return TicketLifecycleDto.builder()
            .ticketLcId(e.getTicketLcId())
            .prevTicketStatus(e.getPrevTicketStatus() != null ? e.getPrevTicketStatus().name() : null)
            .newTicketStatus(e.getNewTicketStatus() != null ? e.getNewTicketStatus().name() : null)
            .statusChangeTime(e.getStatusChangeTime())
            .userEmail(e.getUserEmail())
            .comment(e.getComment())
            .costAggregation(e.getCostAggregation())
            .build();
    }
}

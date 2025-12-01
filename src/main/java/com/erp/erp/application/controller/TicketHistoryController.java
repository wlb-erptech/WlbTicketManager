package com.erp.erp.application.controller;

import com.erp.erp.application.dto.response.TicketLifecycleDto;
import com.erp.erp.application.ticket.TicketHistoryService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketHistoryController {

    private final TicketHistoryService service;

    @GetMapping("/{ticketId}/history")
    public List<TicketLifecycleDto> getTicketHistory(
            @PathVariable Long ticketId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromTs,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toTs
    ) {
        return service.getHistoryList(ticketId, fromTs, toTs);
    }
}

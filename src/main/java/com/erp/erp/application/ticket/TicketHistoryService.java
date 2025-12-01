package com.erp.erp.application.ticket;

import com.erp.erp.application.dto.response.TicketLifecycleDto;
import com.erp.erp.domain.model.ticket.TicketLifeCycleRepository;
import com.erp.erp.domain.model.ticket.TicketLifecycle;
import com.erp.erp.infrastructure.utility.TicketLifecycleMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketHistoryService {

    private final TicketLifeCycleRepository lifecycleRepo;

    @Transactional(readOnly = true)
    public List<TicketLifecycleDto> getHistoryList(
            Long ticketId,
            Instant fromTs,
            Instant toTs
    ) {
        List<TicketLifecycle> list =
            lifecycleRepo.findHistoryList(ticketId, fromTs, toTs);

        return list.stream()
                .map(TicketLifecycleMapper::toDto)
                .toList();
    }
}

package com.erp.erp.application.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AddTicketsToSellCartRequest {
    private List<Long> ticketIds;
    private BigDecimal sellingCost;
}

package com.erp.erp.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketLifecycleDto {

  private Long ticketLcId;
  private String prevTicketStatus;
  private String newTicketStatus;
  private Instant statusChangeTime;
  private String userEmail;
  private String comment;
  private BigDecimal costAggregation;
}

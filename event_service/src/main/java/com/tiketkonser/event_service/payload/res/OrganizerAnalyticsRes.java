package com.tiketkonser.event_service.payload.res;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerAnalyticsRes {
    private BigDecimal totalPendapatan;
    private Integer totalTiketTerjual;
    private Integer totalKuota;
    private Double tingkatKeterisian;
    private Long totalEvent;
    private Long eventAktif;
    private Long eventDraft;
    private Long eventSoldOut;
    private Long eventSelesai;
    private List<EventSalesSummaryRes> eventBreakdowns;
}

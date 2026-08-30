package com.tiketkonser.admin_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryRes {
    private Long totalPengguna;
    private Long totalAcara;
    private Long totalTiketTerjual;
    private BigDecimal totalOmzetPendapatan;
}
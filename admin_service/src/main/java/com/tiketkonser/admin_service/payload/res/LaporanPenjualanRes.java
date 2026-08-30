package com.tiketkonser.admin_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanPenjualanRes {
    private Long id;
    private Long acaraId;
    private Integer totalTiketTerjual;
    private BigDecimal totalPendapatan;
    private String periode;
    private LocalDateTime createdAt;
}
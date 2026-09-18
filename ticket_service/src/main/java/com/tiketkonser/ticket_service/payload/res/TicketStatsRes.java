package com.tiketkonser.ticket_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatsRes {
    private Long acaraId;
    private Integer totalKuota;
    private Integer totalTerjual;
    private Integer totalTersisa;
    private Integer totalUsed;
    private Double persentaseTerjual;
    private List<KategoriStatsItem> perKategori;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KategoriStatsItem {
        private Long kategoriId;
        private String namaKategori;
        private BigDecimal harga;
        private Integer kuota;
        private Integer terjual;
        private Integer tersisa;
        private Integer digunakan;
        private Double persentaseTerjual;
        private String status;
    }
}

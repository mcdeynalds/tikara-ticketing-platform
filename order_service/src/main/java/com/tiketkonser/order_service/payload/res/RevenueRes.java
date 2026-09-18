package com.tiketkonser.order_service.payload.res;

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
public class RevenueRes {
    private Long acaraId;
    private BigDecimal totalRevenue;
    private Long totalOrder;
    private Long totalTiket;
    private List<KategoriRevenueItem> perKategori;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KategoriRevenueItem {
        private Long kategoriTiketId;
        private String namaKategori;
        private Integer jumlahTerjual;
        private BigDecimal hargaSatuan;
        private BigDecimal subtotal;
    }
}

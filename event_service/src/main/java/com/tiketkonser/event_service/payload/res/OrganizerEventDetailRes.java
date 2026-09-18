package com.tiketkonser.event_service.payload.res;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerEventDetailRes {
    private Long acaraId;
    private String namaAcara;
    private String venue;
    private String kota;
    private String status;
    private String gambarUrl;
    private String tanggalMulai;
    private String tanggalSelesai;

    private Integer totalKuota;
    private Integer totalTerjual;
    private Integer totalTersisa;
    private Integer totalUsed;
    private Double persentaseTerjual;
    private List<KategoriStatsItem> perKategoriTiket;

    private BigDecimal totalRevenue;
    private Long totalOrder;
    private Long totalTiketOrder;
    private List<KategoriRevenueItem> revenuePerKategori;

    private Long totalCheckinSukses;
    private Long totalCheckinGagal;
    private Long totalScan;
    private List<TimelineItem> timelineCheckin;

    private Double rataRating;
    private Long jumlahUlasan;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineItem {
        private String jam;
        private Long sukses;
        private Long gagal;
    }
}

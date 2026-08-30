package com.tiketkonser.order_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PemesananRes {
    private Long id;
    private Long penggunaId;
    private String nomorPesanan;
    private BigDecimal totalHarga;
    private String status;
    private LocalDateTime tenggatPembayaran;
    private List<DetailPemesananRes> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
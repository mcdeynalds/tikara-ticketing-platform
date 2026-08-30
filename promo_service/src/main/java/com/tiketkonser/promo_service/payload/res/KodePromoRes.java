package com.tiketkonser.promo_service.payload.res;

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
public class KodePromoRes {
    private Long id;
    private String kode;
    private BigDecimal persenDiskon;
    private BigDecimal potonganMaksimal;
    private BigDecimal minimalTransaksi;
    private Integer kuotaPromo;
    private Integer terpakai;
    private LocalDateTime mulaiBerlaku;
    private LocalDateTime selesaiBerlaku;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
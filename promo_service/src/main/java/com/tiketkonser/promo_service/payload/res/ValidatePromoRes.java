package com.tiketkonser.promo_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatePromoRes {
    private Boolean valid;
    private String kode;
    private BigDecimal potongan;
    private BigDecimal totalHargaAwal;
    private BigDecimal totalHargaAkhir;
    private String message;
}
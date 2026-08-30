package com.tiketkonser.order_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailPemesananRes {
    private Long id;
    private Long kategoriTiketId;
    private Integer jumlah;
    private BigDecimal hargaSatuan;
    private BigDecimal subtotal;
}
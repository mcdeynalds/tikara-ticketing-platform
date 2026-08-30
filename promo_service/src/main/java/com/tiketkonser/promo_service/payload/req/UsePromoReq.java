package com.tiketkonser.promo_service.payload.req;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsePromoReq {

    @NotBlank(message = "Kode promo wajib diisi")
    private String kode;

    @NotNull(message = "Pemesanan ID wajib diisi")
    private Long pemesananId;

    @NotNull(message = "Total harga wajib diisi")
    @DecimalMin(value = "0.0", message = "Total harga minimal 0")
    private BigDecimal totalHarga;
}
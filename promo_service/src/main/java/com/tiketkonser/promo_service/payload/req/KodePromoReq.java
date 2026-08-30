package com.tiketkonser.promo_service.payload.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class KodePromoReq {

    @NotBlank(message = "Kode promo wajib diisi")
    private String kode;

    @NotNull(message = "Persen diskon wajib diisi")
    @DecimalMin(value = "0.01", message = "Persen diskon minimal 0.01%")
    @DecimalMax(value = "100.00", message = "Persen diskon maksimal 100%")
    private BigDecimal persenDiskon;

    @NotNull(message = "Potongan maksimal wajib diisi")
    @DecimalMin(value = "0.0", message = "Potongan maksimal minimal 0")
    private BigDecimal potonganMaksimal;

    @NotNull(message = "Minimal transaksi wajib diisi")
    @DecimalMin(value = "0.0", message = "Minimal transaksi minimal 0")
    private BigDecimal minimalTransaksi;

    @NotNull(message = "Kuota promo wajib diisi")
    @Min(value = 1, message = "Kuota promo minimal 1")
    private Integer kuotaPromo;

    @NotNull(message = "Tanggal mulai berlaku wajib diisi")
    private LocalDateTime mulaiBerlaku;

    @NotNull(message = "Tanggal selesai berlaku wajib diisi")
    private LocalDateTime selesaiBerlaku;
}
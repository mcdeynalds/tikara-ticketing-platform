package com.tiketkonser.ticket_service.payload.req;

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
public class KategoriTiketReq {

    @NotNull(message = "Acara ID wajib diisi")
    private Long acaraId;

    @NotBlank(message = "Nama kategori tiket wajib diisi")
    private String namaKategori;

    @NotNull(message = "Harga wajib diisi")
    @DecimalMin(value = "0.0", message = "Harga minimal 0")
    private BigDecimal harga;

    @NotNull(message = "Kuota wajib diisi")
    @Min(value = 1, message = "Kuota minimal 1")
    private Integer kuota;

    @NotNull(message = "Tanggal mulai jual wajib diisi")
    private LocalDateTime mulaiJual;

    @NotNull(message = "Tanggal selesai jual wajib diisi")
    private LocalDateTime selesaiJual;
}
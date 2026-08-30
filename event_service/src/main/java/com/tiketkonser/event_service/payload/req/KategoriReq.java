package com.tiketkonser.event_service.payload.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KategoriReq {
    @NotBlank(message = "Nama kategori wajib diisi")
    @Size(min = 2, max = 100, message = "Nama kategori minimal 2 karakter dan maksimal 100 karakter")
    private String namaKategori;
}
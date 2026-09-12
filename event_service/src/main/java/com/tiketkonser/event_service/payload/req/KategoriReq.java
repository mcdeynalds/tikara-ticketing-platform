package com.tiketkonser.event_service.payload.req;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KategoriReq {

    @NotBlank(message = "Nama kategori tidak boleh kosong")
    private String namaKategori;

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }
}

package com.tiketkonser.event_service.payload.req;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaraReq {

    @NotBlank(message = "Nama acara tidak boleh kosong")
    private String nama;

    @NotBlank(message = "Deskripsi acara tidak boleh kosong")
    private String deskripsi;

    @NotBlank(message = "Venue tidak boleh kosong")
    private String venue;

    @NotBlank(message = "Kota tidak boleh kosong")
    private String kota;

    @NotNull(message = "Tanggal mulai tidak boleh kosong")
    @Future(message = "Tanggal mulai harus di masa depan")
    private LocalDateTime tanggalMulai;

    @NotNull(message = "Tanggal selesai tidak boleh kosong")
    @Future(message = "Tanggal selesai harus di masa depan")
    private LocalDateTime tanggalSelesai;

    private String gambarUrl;
    private String status;

    private Set<Long> kategoriIds;

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }
    public LocalDateTime getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDateTime tanggalMulai) { this.tanggalMulai = tanggalMulai; }
    public LocalDateTime getTanggalSelesai() { return tanggalSelesai; }
    public void setTanggalSelesai(LocalDateTime tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGambarUrl() { return gambarUrl; }
    public void setGambarUrl(String gambarUrl) { this.gambarUrl = gambarUrl; }
    public Set<Long> getKategoriIds() { return kategoriIds; }
    public void setKategoriIds(Set<Long> kategoriIds) { this.kategoriIds = kategoriIds; }
}

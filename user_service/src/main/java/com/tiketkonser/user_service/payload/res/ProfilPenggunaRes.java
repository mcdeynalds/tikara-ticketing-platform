package com.tiketkonser.user_service.payload.res;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProfilPenggunaRes {
    private Long id;
    private Long penggunaId;
    private String namaLengkap;
    private String email;
    private String peran;
    private String nomorTelepon;
    private String noTelepon;
    private LocalDate tanggalLahir;
    private String alamat;
    private String fotoProfilUrl;
    private String fotoProfil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProfilPenggunaRes() {}

    public ProfilPenggunaRes(Long id, Long penggunaId, String namaLengkap, String email, String peran, String nomorTelepon, String noTelepon, LocalDate tanggalLahir, String alamat, String fotoProfilUrl, String fotoProfil, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.penggunaId = penggunaId;
        this.namaLengkap = namaLengkap;
        this.email = email;
        this.peran = peran;
        this.nomorTelepon = nomorTelepon;
        this.noTelepon = noTelepon;
        this.tanggalLahir = tanggalLahir;
        this.alamat = alamat;
        this.fotoProfilUrl = fotoProfilUrl;
        this.fotoProfil = fotoProfil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPenggunaId() { return penggunaId; }
    public void setPenggunaId(Long penggunaId) { this.penggunaId = penggunaId; }
    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPeran() { return peran; }
    public void setPeran(String peran) { this.peran = peran; }
    public String getNomorTelepon() { return nomorTelepon; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(LocalDate tanggalLahir) { this.tanggalLahir = tanggalLahir; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getFotoProfilUrl() { return fotoProfilUrl; }
    public void setFotoProfilUrl(String fotoProfilUrl) { this.fotoProfilUrl = fotoProfilUrl; }
    public String getFotoProfil() { return fotoProfil; }
    public void setFotoProfil(String fotoProfil) { this.fotoProfil = fotoProfil; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

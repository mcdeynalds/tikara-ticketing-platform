package com.tiketkonser.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profil_pengguna")
@Data
public class ProfilPengguna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pengguna_id", nullable = false)
    private Pengguna pengguna;
    @Column(name = "nomor_telepon")
    private String nomorTelepon;
    @Column(name = "tanggal_lahir")
    private LocalDate tanggalLahir;
    @Column(name = "alamat", columnDefinition = "TEXT")
    private String alamat;
    @Column(name = "foto_profil_url")
    private String fotoProfilUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ProfilPengguna() {
    }

    public ProfilPengguna(Long id, Pengguna pengguna, String nomorTelepon, LocalDate tanggalLahir, String alamat,
            String fotoProfilUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.pengguna = pengguna;
        this.nomorTelepon = nomorTelepon;
        this.tanggalLahir = tanggalLahir;
        this.alamat = alamat;
        this.fotoProfilUrl = fotoProfilUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pengguna getPengguna() {
        return pengguna;
    }

    public void setPengguna(Pengguna pengguna) {
        this.pengguna = pengguna;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }

    public String getNoTelepon() {
        return nomorTelepon;
    }

    public void setNoTelepon(String noTelepon) {
        this.nomorTelepon = noTelepon;
    }

    public LocalDate getTanggalLahir() {
        return tanggalLahir;
    }

    public void setTanggalLahir(LocalDate tanggalLahir) {
        this.tanggalLahir = tanggalLahir;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getFotoProfilUrl() {
        return fotoProfilUrl;
    }

    public void setFotoProfilUrl(String fotoProfilUrl) {
        this.fotoProfilUrl = fotoProfilUrl;
    }

    public String getFotoProfil() {
        return fotoProfilUrl;
    }

    public void setFotoProfil(String fotoProfil) {
        this.fotoProfilUrl = fotoProfil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

package com.tiketkonser.user_service.payload.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfilReq {

    @Size(min = 3, max = 60, message = "Nama harus antara 3 hingga 60 karakter")
    @JsonAlias({"namaLengkap", "fullName", "name"})
    private String nama;

    @Size(min = 3, max = 60, message = "Nama harus antara 3 hingga 60 karakter")
    private String namaLengkap;

    @JsonAlias({"phone", "telepon", "phoneNumber"})
    private String nomorTelepon;

    private String noTelepon;

    private LocalDate tanggalLahir;

    private String alamat;

    @JsonAlias({"avatarUrl", "avatar", "photoUrl"})
    private String fotoProfilUrl;

    private String fotoProfil;

    public String getNama() {
        if (nama != null && !nama.isBlank()) return nama;
        if (namaLengkap != null && !namaLengkap.isBlank()) return namaLengkap;
        return null;
    }

    public void setNama(String nama) {
        this.nama = nama;
        if (this.namaLengkap == null) this.namaLengkap = nama;
    }

    public String getNamaLengkap() {
        return getNama();
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
        if (this.nama == null) this.nama = namaLengkap;
    }

    public String getNomorTelepon() {
        if (nomorTelepon != null && !nomorTelepon.isBlank()) return nomorTelepon;
        if (noTelepon != null && !noTelepon.isBlank()) return noTelepon;
        return null;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
        if (this.noTelepon == null) this.noTelepon = nomorTelepon;
    }

    public String getNoTelepon() {
        return getNomorTelepon();
    }

    public void setNoTelepon(String noTelepon) {
        this.noTelepon = noTelepon;
        if (this.nomorTelepon == null) this.nomorTelepon = noTelepon;
    }

    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(LocalDate tanggalLahir) { this.tanggalLahir = tanggalLahir; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getFotoProfilUrl() {
        if (fotoProfilUrl != null && !fotoProfilUrl.isBlank()) return fotoProfilUrl;
        if (fotoProfil != null && !fotoProfil.isBlank()) return fotoProfil;
        return null;
    }

    public void setFotoProfilUrl(String fotoProfilUrl) {
        this.fotoProfilUrl = fotoProfilUrl;
        if (this.fotoProfil == null) this.fotoProfil = fotoProfilUrl;
    }

    public String getFotoProfil() {
        return getFotoProfilUrl();
    }

    public void setFotoProfil(String fotoProfil) {
        this.fotoProfil = fotoProfil;
        if (this.fotoProfilUrl == null) this.fotoProfilUrl = fotoProfil;
    }
}

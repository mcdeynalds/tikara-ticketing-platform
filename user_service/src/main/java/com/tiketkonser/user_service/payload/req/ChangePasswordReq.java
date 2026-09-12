package com.tiketkonser.user_service.payload.req;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class ChangePasswordReq {

    @NotBlank(message = "Kata sandi lama wajib diisi")
    @JsonAlias({"oldPassword", "currentPassword"})
    private String kataSandiLama;

    @NotBlank(message = "Kata sandi baru wajib diisi")
    @Size(min = 6, message = "Kata sandi baru minimal 6 karakter")
    @JsonAlias({"newPassword"})
    private String kataSandiBaru;

    @JsonAlias({"confirmNewPassword", "confirmPassword"})
    private String konfirmasiKataSandiBaru;

    public String getKataSandiLama() { return kataSandiLama; }
    public void setKataSandiLama(String kataSandiLama) { this.kataSandiLama = kataSandiLama; }
    public String getKataSandiBaru() { return kataSandiBaru; }
    public void setKataSandiBaru(String kataSandiBaru) { this.kataSandiBaru = kataSandiBaru; }
    public String getKonfirmasiKataSandiBaru() {
        if (konfirmasiKataSandiBaru != null && !konfirmasiKataSandiBaru.isBlank()) {
            return konfirmasiKataSandiBaru;
        }
        return kataSandiBaru;
    }
    public void setKonfirmasiKataSandiBaru(String konfirmasiKataSandiBaru) { this.konfirmasiKataSandiBaru = konfirmasiKataSandiBaru; }
}

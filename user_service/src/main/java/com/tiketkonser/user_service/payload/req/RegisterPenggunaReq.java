package com.tiketkonser.user_service.payload.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPenggunaReq {
    @NotBlank(message = "Nama tidak boleh kosong") private String nama;
    @NotBlank(message = "Email tidak boleh kosong") @Email(message = "Format email tidak valid") private String email;
    @NotBlank(message = "Kata sandi tidak boleh kosong") @Size(min = 8, message = "Kata sandi minimal 8 karakter") private String kataSandi;
    @NotBlank(message = "Role wajib dipilih") private String role;
    private String nomorTelepon;

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getKataSandi() { return kataSandi; }
    public void setKataSandi(String kataSandi) { this.kataSandi = kataSandi; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getNomorTelepon() { return nomorTelepon; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }
}

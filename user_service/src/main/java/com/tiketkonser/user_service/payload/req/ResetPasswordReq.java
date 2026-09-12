package com.tiketkonser.user_service.payload.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordReq {

    @NotBlank(message = "Token tidak boleh kosong")
    @JsonAlias({"resetToken", "reset_token"})
    private String token;

    @NotBlank(message = "Kata sandi baru tidak boleh kosong")
    @Size(min = 6, message = "Kata sandi minimal 6 karakter")
    @JsonAlias({"newPassword", "password", "kata_sandi_baru"})
    private String kataSandiBaru;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getKataSandiBaru() { return kataSandiBaru; }
    public void setKataSandiBaru(String kataSandiBaru) { this.kataSandiBaru = kataSandiBaru; }
}

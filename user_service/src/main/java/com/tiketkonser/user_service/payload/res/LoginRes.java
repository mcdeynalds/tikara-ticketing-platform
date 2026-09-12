package com.tiketkonser.user_service.payload.res;

import lombok.*;

@Data
public class LoginRes {
    private Long id;
    private String nama;
    private String email;
    private String peran;
    private String accessToken;
    private String refreshToken;
    private String tokenType;

    public LoginRes() {}

    public LoginRes(Long id, String nama, String email, String peran, String accessToken, String refreshToken, String tokenType) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.peran = peran;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPeran() { return peran; }
    public void setPeran(String peran) { this.peran = peran; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}

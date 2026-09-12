package com.tiketkonser.user_service.payload.res;

import lombok.*;
import java.time.LocalDateTime;

@Data
public class PenggunaRes {
    private Long id;
    private String nama;
    private String email;
    private String peran;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PenggunaRes() {}

    public PenggunaRes(Long id, String nama, String email, String peran, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.peran = peran;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPeran() { return peran; }
    public void setPeran(String peran) { this.peran = peran; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

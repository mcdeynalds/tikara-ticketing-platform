package com.tiketkonser.user_service.utility;

import com.tiketkonser.user_service.entity.Pengguna;
import com.tiketkonser.user_service.payload.req.RegisterPenggunaReq;
import com.tiketkonser.user_service.payload.res.PenggunaRes;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PenggunaMapper {

    public Pengguna toEntity(RegisterPenggunaReq request) {
        return new Pengguna(null, request.getNama(), request.getEmail(), "", request.getRole() != null ? request.getRole().toUpperCase() : "PEMBELI", LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public PenggunaRes toRes(Pengguna pengguna) {
        return new PenggunaRes(pengguna.getId(), pengguna.getNama(), pengguna.getEmail(), pengguna.getPeran(), pengguna.getCreatedAt(), pengguna.getUpdatedAt());
    }
}

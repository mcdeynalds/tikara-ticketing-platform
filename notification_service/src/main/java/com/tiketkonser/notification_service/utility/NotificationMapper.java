package com.tiketkonser.notification_service.utility;

import com.tiketkonser.notification_service.entity.Notifikasi;
import com.tiketkonser.notification_service.payload.res.NotifikasiRes;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotifikasiRes toNotifikasiRes(Notifikasi entity) {
        if (entity == null) return null;
        return NotifikasiRes.builder()
                .id(entity.getId())
                .penggunaId(entity.getPenggunaId())
                .judul(entity.getJudul())
                .pesan(entity.getPesan())
                .tipe(entity.getTipe())
                .status(entity.getStatus())
                .dibaca(entity.getDibaca())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
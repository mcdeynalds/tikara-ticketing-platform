package com.tiketkonser.checkin_service.utility;

import com.tiketkonser.checkin_service.entity.RiwayatCheckin;
import com.tiketkonser.checkin_service.payload.res.ScanTicketRes;
import org.springframework.stereotype.Component;

@Component
public class CheckinMapper {

    public ScanTicketRes toScanTicketRes(RiwayatCheckin entity, String message) {
        if (entity == null) return null;
        return ScanTicketRes.builder()
                .id(entity.getId())
                .acaraId(entity.getAcaraId())
                .tiketId(entity.getTiketId())
                .kodeQr(entity.getKodeQr())
                .petugasId(entity.getPetugasId())
                .lokasiGate(entity.getLokasiGate())
                .statusCheckin(entity.getStatusCheckin())
                .message(message)
                .waktuCheckin(entity.getWaktuCheckin())
                .build();
    }
}
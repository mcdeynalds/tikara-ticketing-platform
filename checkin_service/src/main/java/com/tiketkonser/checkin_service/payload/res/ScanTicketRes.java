package com.tiketkonser.checkin_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanTicketRes {
    private Long id;
    private Long acaraId;
    private Long tiketId;
    private String kodeQr;
    private Long petugasId;
    private String lokasiGate;
    private String statusCheckin;
    private String message;
    private LocalDateTime waktuCheckin;
}
package com.tiketkonser.ticket_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferTiketRes {
    private String status;
    private String message;
    private Long oldTiketId;
    private Long newTiketId;
    private String newKodeQr;
    private Long penerimaPenggunaId;
    private String penerimaEmail;
    private String penerimaNama;
    private LocalDateTime transferAt;
}

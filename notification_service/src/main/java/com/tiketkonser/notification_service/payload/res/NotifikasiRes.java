package com.tiketkonser.notification_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifikasiRes {
    private Long id;
    private Long penggunaId;
    private String judul;
    private String pesan;
    private String tipe;
    private String status;
    private Boolean dibaca;
    private LocalDateTime createdAt;
}
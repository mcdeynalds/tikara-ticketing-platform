package com.tiketkonser.queue_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusRes {
    private Long acaraId;
    private Long penggunaId;
    private Integer nomorAntrean;
    private Integer posisiAntrean;
    private Long totalMenunggu;
    private String status;
    private Integer estimasiMenit;
    private LocalDateTime masukAt;
}
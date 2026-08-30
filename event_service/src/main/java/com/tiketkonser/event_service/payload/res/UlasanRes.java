package com.tiketkonser.event_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UlasanRes {
    private Long id;
    private Long acaraId;
    private String acaraNama;
    private Long penggunaId;
    private String penggunaNama;
    private Integer rating;
    private String komentar;
    private LocalDateTime createdAt;
}
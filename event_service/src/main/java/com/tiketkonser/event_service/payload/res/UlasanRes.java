package com.tiketkonser.event_service.payload.res;

import lombok.*;
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

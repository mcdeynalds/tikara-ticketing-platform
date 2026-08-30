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
public class KategoriRes {
    private Long id;
    private String namaKategori;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
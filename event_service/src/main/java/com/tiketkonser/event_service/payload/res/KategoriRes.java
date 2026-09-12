package com.tiketkonser.event_service.payload.res;

import lombok.*;
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

    // Alias for frontend compatibility (matches both .nama and .namaKategori)
    public String getNama() {
        return namaKategori;
    }
}

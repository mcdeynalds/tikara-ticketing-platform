package com.tiketkonser.event_service.payload.res;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSalesSummaryRes {
    private Long id;
    private String nama;
    private String venue;
    private String kota;
    private String status;
    private String gambarUrl;
    private LocalDateTime tanggalMulai;
    private Integer kuota;
    private Integer terjual;
    private Integer sisa;
    private Double keterisianPersen;
    private BigDecimal pendapatan;
}

package com.tiketkonser.checkin_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinStatsRes {
    private Long acaraId;
    private Long totalCheckinSuccess;
    private Long totalCheckinFailed;
    private Long totalScanCount;
}
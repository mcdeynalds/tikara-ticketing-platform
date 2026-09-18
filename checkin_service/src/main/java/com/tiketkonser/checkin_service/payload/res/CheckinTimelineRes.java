package com.tiketkonser.checkin_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinTimelineRes {
    private Long acaraId;
    private Long totalCheckinSuccess;
    private Long totalCheckinFailed;
    private Long totalScanCount;
    private List<TimelineItem> timelinePerJam;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineItem {
        private String jam;
        private Long sukses;
        private Long gagal;
    }
}

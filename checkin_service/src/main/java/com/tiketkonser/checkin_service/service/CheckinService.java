package com.tiketkonser.checkin_service.service;

import com.tiketkonser.checkin_service.payload.req.ScanTicketReq;
import com.tiketkonser.checkin_service.payload.res.CheckinStatsRes;
import com.tiketkonser.checkin_service.payload.res.PageResponse;
import com.tiketkonser.checkin_service.payload.res.ScanTicketRes;
import org.springframework.security.core.Authentication;

public interface CheckinService {
    ScanTicketRes scanTicket(ScanTicketReq request, Authentication authentication);
    PageResponse<ScanTicketRes> getHistoryByAcaraId(Long acaraId, int page, int size);
    CheckinStatsRes getStatsByAcaraId(Long acaraId);
}
package com.tiketkonser.ticket_service.service;

import com.tiketkonser.ticket_service.payload.req.IssueTiketReq;
import com.tiketkonser.ticket_service.payload.res.PageResponse;
import com.tiketkonser.ticket_service.payload.res.TiketRes;
import org.springframework.security.core.Authentication;

public interface TiketService {
    TiketRes issueTiket(IssueTiketReq request);
    PageResponse<TiketRes> getMyTiket(Authentication authentication, int page, int size);
    TiketRes getByKodeQr(String kodeQr);
    TiketRes validateOrUseTiket(String kodeQr);
}
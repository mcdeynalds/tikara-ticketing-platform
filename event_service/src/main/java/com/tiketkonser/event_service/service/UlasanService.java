package com.tiketkonser.event_service.service;

import com.tiketkonser.event_service.payload.req.UlasanReq;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.payload.res.UlasanRes;
import org.springframework.security.core.Authentication;

public interface UlasanService {
    UlasanRes create(Long acaraId, UlasanReq request, Authentication authentication);
    PageResponse<UlasanRes> getByAcaraId(Long acaraId, int page, int size);
}
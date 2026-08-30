package com.tiketkonser.event_service.service;

import com.tiketkonser.event_service.payload.req.AcaraReq;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.PageResponse;
import org.springframework.security.core.Authentication;

public interface AcaraService {
    AcaraRes create(AcaraReq request, Authentication authentication);
    AcaraRes getById(Long id);
    PageResponse<AcaraRes> getAll(String search, String kota, String status, int page, int size, String sortBy, String sortDir);
    AcaraRes update(Long id, AcaraReq request, Authentication authentication);
    void delete(Long id, Authentication authentication);
    AcaraRes moderate(Long id, String status, Authentication authentication);
}
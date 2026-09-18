package com.tiketkonser.event_service.service;

import com.tiketkonser.event_service.payload.req.AcaraReq;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.OrganizerAnalyticsRes;
import com.tiketkonser.event_service.payload.res.OrganizerEventDetailRes;
import com.tiketkonser.event_service.payload.res.PageResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface AcaraService {

    AcaraRes create(AcaraReq request, Authentication authentication);

    PageResponse<AcaraRes> getAll(String search, String kota, String status, int page, int size, String sortBy,
            String sortDir);

    PageResponse<AcaraRes> getAll(String search, String kota, Long kategoriId, String status, int page, int size,
            String sortBy, String sortDir, Authentication authentication);

    PageResponse<AcaraRes> getAll(String search, String kota, Long kategoriId, String kategoriNama, String status,
            int page, int size, String sortBy, String sortDir, Authentication authentication);

    PageResponse<AcaraRes> getMyEvents(String search, String status, int page, int size, String sortBy, String sortDir,
            Authentication authentication);

    OrganizerAnalyticsRes getOrganizerAnalytics(Authentication authentication);

    AcaraRes changeStatus(Long id, String status, Authentication authentication);

    AcaraRes getById(Long id);

    AcaraRes getById(Long id, Authentication authentication);

    AcaraRes update(Long id, AcaraReq request, Authentication authentication);

    void delete(Long id, Authentication authentication);

    AcaraRes restore(Long id);

    AcaraRes uploadPoster(Long id, MultipartFile file);

    AcaraRes uploadFlyerPdf(Long id, MultipartFile file);

    AcaraRes moderate(Long id, String status, Authentication authentication);

    PageResponse<AcaraRes> getPendingReview(int page, int size, Authentication authentication);

    AcaraRes moderateEvent(Long id, String action, String catatan, Authentication authentication);

    AcaraRes resubmitEvent(Long id, Authentication authentication);

    OrganizerEventDetailRes getEventDetailForOrganizer(Long acaraId, Authentication authentication, String authHeader);
}

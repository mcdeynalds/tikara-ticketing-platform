package com.tiketkonser.ticket_service.service;

import com.tiketkonser.ticket_service.payload.req.IssueTiketReq;
import com.tiketkonser.ticket_service.payload.req.TransferTiketReq;
import com.tiketkonser.ticket_service.payload.res.ManifestItemRes;
import com.tiketkonser.ticket_service.payload.res.PageResponse;
import com.tiketkonser.ticket_service.payload.res.TicketStatsRes;
import com.tiketkonser.ticket_service.payload.res.TiketRes;
import com.tiketkonser.ticket_service.payload.res.TransferTiketRes;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TiketService {
    TiketRes issueTiket(IssueTiketReq request);
    PageResponse<TiketRes> getMyTiket(Authentication authentication, int page, int size);
    TiketRes getByKodeQr(String kodeQr);
    TiketRes validateOrUseTiket(String kodeQr);
    TiketRes validateOrUseTiket(String kodeQr, Long acaraId);
    TransferTiketRes transferTiket(Long tiketId, TransferTiketReq request, Authentication authentication);
    default TransferTiketRes transferTiket(Long tiketId, TransferTiketReq request, Authentication authentication, String authHeader) {
        return transferTiket(tiketId, request, authentication);
    }
    void refundTiketByDetailPemesananId(Long detailPemesananId);
    boolean hasTicketForEvent(Long penggunaId, Long acaraId);

    TicketStatsRes getTicketStatsByAcara(Long acaraId);
    List<ManifestItemRes> getManifestByAcara(Long acaraId);
}

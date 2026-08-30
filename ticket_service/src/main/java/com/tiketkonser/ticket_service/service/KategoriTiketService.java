package com.tiketkonser.ticket_service.service;

import com.tiketkonser.ticket_service.payload.req.HoldTiketReq;
import com.tiketkonser.ticket_service.payload.req.KategoriTiketReq;
import com.tiketkonser.ticket_service.payload.res.KategoriTiketRes;

import java.util.List;

public interface KategoriTiketService {
    KategoriTiketRes create(KategoriTiketReq request);
    List<KategoriTiketRes> getByAcaraId(Long acaraId);
    KategoriTiketRes getById(Long id);
    KategoriTiketRes update(Long id, KategoriTiketReq request);
    void holdStok(HoldTiketReq request);
    void releaseStok(HoldTiketReq request);
    void confirmStok(HoldTiketReq request);
}
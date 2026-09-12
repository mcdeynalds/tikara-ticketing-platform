package com.tiketkonser.event_service.service;

import com.tiketkonser.event_service.payload.req.KategoriReq;
import com.tiketkonser.event_service.payload.res.KategoriRes;
import java.util.List;

public interface KategoriService {
    KategoriRes create(KategoriReq request);
    List<KategoriRes> getAll();
    KategoriRes getById(Long id);
}

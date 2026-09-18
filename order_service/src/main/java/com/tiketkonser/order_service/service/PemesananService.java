package com.tiketkonser.order_service.service;

import com.tiketkonser.order_service.payload.req.PemesananReq;
import com.tiketkonser.order_service.payload.res.PageResponse;
import com.tiketkonser.order_service.payload.res.PemesananRes;
import com.tiketkonser.order_service.payload.res.RevenueRes;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PemesananService {
    PemesananRes createOrder(PemesananReq request, Authentication authentication);

    PemesananRes createOrder(PemesananReq request, Authentication authentication, String authHeader);

    PageResponse<PemesananRes> getMyOrders(Authentication authentication, int page, int size);

    PemesananRes getOrderById(Long id, Authentication authentication);

    PemesananRes cancelOrder(Long id, Authentication authentication);

    PemesananRes paySimulation(Long id, Authentication authentication);

    PemesananRes paySimulation(Long id, Authentication authentication, String authHeader);

    RevenueRes getRevenueByAcara(Long acaraId, List<Long> kategoriTiketIds);
}

package com.tiketkonser.order_service.service;

import com.tiketkonser.order_service.payload.req.PemesananReq;
import com.tiketkonser.order_service.payload.res.PageResponse;
import com.tiketkonser.order_service.payload.res.PemesananRes;
import org.springframework.security.core.Authentication;

public interface PemesananService {
    PemesananRes createOrder(PemesananReq request, Authentication authentication);
    PageResponse<PemesananRes> getMyOrders(Authentication authentication, int page, int size);
    PemesananRes getOrderById(Long id, Authentication authentication);
    PemesananRes cancelOrder(Long id, Authentication authentication);
    PemesananRes paySimulation(Long id, Authentication authentication);
}
package com.tiketkonser.promo_service.service;

import com.tiketkonser.promo_service.payload.req.KodePromoReq;
import com.tiketkonser.promo_service.payload.req.UsePromoReq;
import com.tiketkonser.promo_service.payload.req.ValidatePromoReq;
import com.tiketkonser.promo_service.payload.res.KodePromoRes;
import com.tiketkonser.promo_service.payload.res.ValidatePromoRes;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

public interface PromoService {
    KodePromoRes createPromo(KodePromoReq request);
    List<KodePromoRes> getAllActivePromo();
    List<KodePromoRes> getAllPromos();
    KodePromoRes getById(Long id);
    KodePromoRes togglePromoStatus(Long id);
    KodePromoRes deactivatePromo(Long id);
    Map<String, Object> getPromoStats();
    ValidatePromoRes validatePromo(ValidatePromoReq request, Authentication authentication);
    ValidatePromoRes usePromo(UsePromoReq request, Authentication authentication);
    Map<String, Object> cancelPromoUsage(Long pemesananId, Authentication authentication);
}

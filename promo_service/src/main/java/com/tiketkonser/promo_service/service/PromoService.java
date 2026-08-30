package com.tiketkonser.promo_service.service;

import com.tiketkonser.promo_service.payload.req.KodePromoReq;
import com.tiketkonser.promo_service.payload.req.UsePromoReq;
import com.tiketkonser.promo_service.payload.req.ValidatePromoReq;
import com.tiketkonser.promo_service.payload.res.KodePromoRes;
import com.tiketkonser.promo_service.payload.res.ValidatePromoRes;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PromoService {
    KodePromoRes createPromo(KodePromoReq request);
    List<KodePromoRes> getAllActivePromo();
    KodePromoRes getById(Long id);
    ValidatePromoRes validatePromo(ValidatePromoReq request, Authentication authentication);
    ValidatePromoRes usePromo(UsePromoReq request, Authentication authentication);
}
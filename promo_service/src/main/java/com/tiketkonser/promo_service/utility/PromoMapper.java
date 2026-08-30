package com.tiketkonser.promo_service.utility;

import com.tiketkonser.promo_service.entity.KodePromo;
import com.tiketkonser.promo_service.payload.res.KodePromoRes;
import org.springframework.stereotype.Component;

@Component
public class PromoMapper {

    public KodePromoRes toKodePromoRes(KodePromo entity) {
        if (entity == null) return null;
        return KodePromoRes.builder()
                .id(entity.getId())
                .kode(entity.getKode())
                .persenDiskon(entity.getPersenDiskon())
                .potonganMaksimal(entity.getPotonganMaksimal())
                .minimalTransaksi(entity.getMinimalTransaksi())
                .kuotaPromo(entity.getKuotaPromo())
                .terpakai(entity.getTerpakai())
                .mulaiBerlaku(entity.getMulaiBerlaku())
                .selesaiBerlaku(entity.getSelesaiBerlaku())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
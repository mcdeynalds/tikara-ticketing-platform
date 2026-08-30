package com.tiketkonser.promo_service.controller;

import com.tiketkonser.promo_service.payload.req.KodePromoReq;
import com.tiketkonser.promo_service.payload.req.UsePromoReq;
import com.tiketkonser.promo_service.payload.req.ValidatePromoReq;
import com.tiketkonser.promo_service.payload.res.KodePromoRes;
import com.tiketkonser.promo_service.payload.res.ValidatePromoRes;
import com.tiketkonser.promo_service.service.PromoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promo")
@RequiredArgsConstructor
public class PromoController {

    private final PromoService promoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KodePromoRes> createPromo(@Valid @RequestBody KodePromoReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promoService.createPromo(request));
    }

    @GetMapping
    public ResponseEntity<List<KodePromoRes>> getAllActivePromo() {
        return ResponseEntity.ok(promoService.getAllActivePromo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KodePromoRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promoService.getById(id));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidatePromoRes> validatePromo(@Valid @RequestBody ValidatePromoReq request, Authentication authentication) {
        return ResponseEntity.ok(promoService.validatePromo(request, authentication));
    }

    @PostMapping("/use")
    public ResponseEntity<ValidatePromoRes> usePromo(@Valid @RequestBody UsePromoReq request, Authentication authentication) {
        return ResponseEntity.ok(promoService.usePromo(request, authentication));
    }
}
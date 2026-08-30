package com.tiketkonser.order_service.controller;

import com.tiketkonser.order_service.payload.req.PemesananReq;
import com.tiketkonser.order_service.payload.res.PageResponse;
import com.tiketkonser.order_service.payload.res.PemesananRes;
import com.tiketkonser.order_service.service.PemesananService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PemesananController {

    private final PemesananService pemesananService;

    @PostMapping
    public ResponseEntity<PemesananRes> createOrder(@Valid @RequestBody PemesananReq request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pemesananService.createOrder(request, authentication));
    }

    @GetMapping("/saya")
    public ResponseEntity<PageResponse<PemesananRes>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(pemesananService.getMyOrders(authentication, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PemesananRes> getOrderById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(pemesananService.getOrderById(id, authentication));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PemesananRes> cancelOrder(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(pemesananService.cancelOrder(id, authentication));
    }

    @PostMapping("/{id}/pay-simulation")
    public ResponseEntity<PemesananRes> paySimulation(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(pemesananService.paySimulation(id, authentication));
    }
}
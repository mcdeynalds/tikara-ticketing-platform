package com.tiketkonser.ticket_service.controller;

import com.tiketkonser.ticket_service.payload.req.IssueTiketReq;
import com.tiketkonser.ticket_service.payload.res.PageResponse;
import com.tiketkonser.ticket_service.payload.res.TiketRes;
import com.tiketkonser.ticket_service.service.TiketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tiket")
@RequiredArgsConstructor
public class TiketController {

    private final TiketService tiketService;

    @PostMapping("/issue")
    public ResponseEntity<TiketRes> issueTiket(@Valid @RequestBody IssueTiketReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tiketService.issueTiket(request));
    }

    @GetMapping("/saya")
    public ResponseEntity<PageResponse<TiketRes>> getMyTiket(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tiketService.getMyTiket(authentication, page, size));
    }

    @GetMapping("/qr/{kodeQr}")
    public ResponseEntity<TiketRes> getByKodeQr(@PathVariable String kodeQr) {
        return ResponseEntity.ok(tiketService.getByKodeQr(kodeQr));
    }

    @PostMapping("/check-in/{kodeQr}")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<TiketRes> checkIn(@PathVariable String kodeQr) {
        return ResponseEntity.ok(tiketService.validateOrUseTiket(kodeQr));
    }
}
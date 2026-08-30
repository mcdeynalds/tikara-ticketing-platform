package com.tiketkonser.ticket_service.controller;

import com.tiketkonser.ticket_service.payload.req.HoldTiketReq;
import com.tiketkonser.ticket_service.service.KategoriTiketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final KategoriTiketService kategoriTiketService;

    @PostMapping("/hold")
    public ResponseEntity<Map<String, String>> holdStok(@Valid @RequestBody HoldTiketReq request) {
        kategoriTiketService.holdStok(request);
        return ResponseEntity.ok(Map.of("message", "Stok tiket berhasil direservasi sementara"));
    }

    @PostMapping("/release")
    public ResponseEntity<Map<String, String>> releaseStok(@Valid @RequestBody HoldTiketReq request) {
        kategoriTiketService.releaseStok(request);
        return ResponseEntity.ok(Map.of("message", "Reservasi stok tiket berhasil dilepaskan"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmStok(@Valid @RequestBody HoldTiketReq request) {
        kategoriTiketService.confirmStok(request);
        return ResponseEntity.ok(Map.of("message", "Stok tiket berhasil dikonfirmasi terjual"));
    }
}
package com.tiketkonser.ticket_service.controller;

import com.tiketkonser.ticket_service.payload.req.KategoriTiketReq;
import com.tiketkonser.ticket_service.payload.res.KategoriTiketRes;
import com.tiketkonser.ticket_service.service.KategoriTiketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kategori-tiket")
@RequiredArgsConstructor
public class KategoriTiketController {

    private final KategoriTiketService kategoriTiketService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<KategoriTiketRes> create(@Valid @RequestBody KategoriTiketReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kategoriTiketService.create(request));
    }

    @GetMapping("/acara/{acaraId}")
    public ResponseEntity<List<KategoriTiketRes>> getByAcaraId(@PathVariable Long acaraId) {
        return ResponseEntity.ok(kategoriTiketService.getByAcaraId(acaraId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KategoriTiketRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kategoriTiketService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<KategoriTiketRes> update(@PathVariable Long id, @Valid @RequestBody KategoriTiketReq request) {
        return ResponseEntity.ok(kategoriTiketService.update(id, request));
    }
}
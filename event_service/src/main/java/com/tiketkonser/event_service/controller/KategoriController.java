package com.tiketkonser.event_service.controller;

import com.tiketkonser.event_service.payload.req.KategoriReq;
import com.tiketkonser.event_service.payload.res.KategoriRes;
import com.tiketkonser.event_service.service.KategoriService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kategori")
@RequiredArgsConstructor
public class KategoriController {

    private final KategoriService kategoriService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KategoriRes> create(@Valid @RequestBody KategoriReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kategoriService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<KategoriRes>> getAll() {
        return ResponseEntity.ok(kategoriService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KategoriRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kategoriService.getById(id));
    }
}
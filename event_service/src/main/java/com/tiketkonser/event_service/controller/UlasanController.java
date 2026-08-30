package com.tiketkonser.event_service.controller;

import com.tiketkonser.event_service.payload.req.UlasanReq;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.payload.res.UlasanRes;
import com.tiketkonser.event_service.service.UlasanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/acara")
@RequiredArgsConstructor
public class UlasanController {

    private final UlasanService ulasanService;

    @PostMapping("/{acaraId}/ulasan")
    public ResponseEntity<UlasanRes> create(
            @PathVariable Long acaraId,
            @Valid @RequestBody UlasanReq request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ulasanService.create(acaraId, request, authentication));
    }

    @GetMapping("/{acaraId}/ulasan")
    public ResponseEntity<PageResponse<UlasanRes>> getByAcaraId(
            @PathVariable Long acaraId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ulasanService.getByAcaraId(acaraId, page, size));
    }
}
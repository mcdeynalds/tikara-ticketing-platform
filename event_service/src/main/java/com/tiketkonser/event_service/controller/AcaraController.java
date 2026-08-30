package com.tiketkonser.event_service.controller;

import com.tiketkonser.event_service.payload.req.AcaraReq;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.service.AcaraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/acara")
@RequiredArgsConstructor
public class AcaraController {

    private final AcaraService acaraService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<AcaraRes> create(@Valid @RequestBody AcaraReq request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(acaraService.create(request, authentication));
    }

    @GetMapping
    public ResponseEntity<PageResponse<AcaraRes>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String kota,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(acaraService.getAll(search, kota, status, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcaraRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(acaraService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<AcaraRes> update(@PathVariable Long id, @Valid @RequestBody AcaraReq request, Authentication authentication) {
        return ResponseEntity.ok(acaraService.update(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        acaraService.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcaraRes> moderate(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        return ResponseEntity.ok(acaraService.moderate(id, status, authentication));
    }
}
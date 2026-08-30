package com.tiketkonser.checkin_service.controller;

import com.tiketkonser.checkin_service.payload.req.ScanTicketReq;
import com.tiketkonser.checkin_service.payload.res.CheckinStatsRes;
import com.tiketkonser.checkin_service.payload.res.PageResponse;
import com.tiketkonser.checkin_service.payload.res.ScanTicketRes;
import com.tiketkonser.checkin_service.service.CheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<ScanTicketRes> scanTicket(@Valid @RequestBody ScanTicketReq request, Authentication authentication) {
        return ResponseEntity.ok(checkinService.scanTicket(request, authentication));
    }

    @GetMapping("/history/{acaraId}")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<PageResponse<ScanTicketRes>> getHistoryByAcaraId(
            @PathVariable Long acaraId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(checkinService.getHistoryByAcaraId(acaraId, page, size));
    }

    @GetMapping("/stats/{acaraId}")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<CheckinStatsRes> getStatsByAcaraId(@PathVariable Long acaraId) {
        return ResponseEntity.ok(checkinService.getStatsByAcaraId(acaraId));
    }
}
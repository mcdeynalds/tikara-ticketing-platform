package com.tiketkonser.ticket_service.controller;

import com.tiketkonser.ticket_service.payload.req.IssueTiketReq;
import com.tiketkonser.ticket_service.payload.req.TransferTiketReq;
import com.tiketkonser.ticket_service.payload.res.PageResponse;
import com.tiketkonser.ticket_service.payload.res.ManifestItemRes;
import com.tiketkonser.ticket_service.payload.res.TicketStatsRes;
import com.tiketkonser.ticket_service.payload.res.TiketRes;
import com.tiketkonser.ticket_service.payload.res.TransferTiketRes;
import com.tiketkonser.ticket_service.service.EmailTicketService;
import com.tiketkonser.ticket_service.service.PdfTicketService;
import com.tiketkonser.ticket_service.service.TiketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tiket")
@RequiredArgsConstructor
public class TiketController {

    private final TiketService tiketService;
    private final PdfTicketService pdfTicketService;
    private final EmailTicketService emailTicketService;

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
    public ResponseEntity<TiketRes> checkIn(
            @PathVariable String kodeQr,
            @RequestParam(required = false) Long acaraId) {
        return ResponseEntity.ok(tiketService.validateOrUseTiket(kodeQr, acaraId));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<TransferTiketRes> transferTiket(
            @PathVariable Long id,
            @Valid @RequestBody TransferTiketReq request,
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return ResponseEntity.ok(tiketService.transferTiket(id, request, authentication, authHeader));
    }

    @GetMapping(value = {"/{id}/pdf", "/{id}/download-pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdfTicket(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "token", required = false) String queryToken,
            @RequestParam(value = "download", required = false, defaultValue = "false") boolean download) {

        String effectiveAuth = authHeader;
        if ((effectiveAuth == null || effectiveAuth.isBlank()) && queryToken != null && !queryToken.isBlank()) {
            effectiveAuth = "Bearer " + queryToken.trim();
        }

        byte[] pdfBytes = pdfTicketService.generateTicketPdf(id, authentication, effectiveAuth);

        org.springframework.http.ContentDisposition disposition = download
                ? org.springframework.http.ContentDisposition.attachment().filename("TIKARA_ETicket_TKT" + id + ".pdf").build()
                : org.springframework.http.ContentDisposition.inline().filename("TIKARA_ETicket_TKT" + id + ".pdf").build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(disposition);
        headers.setContentLength(pdfBytes.length);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<Map<String, Object>> sendTicketEmail(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        String targetEmail = requestBody != null ? requestBody.get("email") : null;
        emailTicketService.sendTicketEmail(id, authentication, authHeader, targetEmail);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "E-Tiket resmi dengan lampiran PDF berhasil dikirimkan ke email Anda!"
        ));
    }

    @PostMapping("/refund-by-detail/{detailId}")
    public ResponseEntity<Map<String, String>> refundTiketByDetail(@PathVariable Long detailId) {
        tiketService.refundTiketByDetailPemesananId(detailId);
        return ResponseEntity.ok(Map.of("message", "Tiket untuk detail pesanan berhasil di-refund"));
    }


    @GetMapping("/has-ticket")
    public ResponseEntity<Map<String, Object>> hasTicketForEvent(
            @RequestParam Long penggunaId,
            @RequestParam Long acaraId) {
        boolean hasTicket = tiketService.hasTicketForEvent(penggunaId, acaraId);
        return ResponseEntity.ok(Map.of(
                "hasTicket", hasTicket,
                "penggunaId", penggunaId,
                "acaraId", acaraId
        ));
    }

    @GetMapping("/acara/{acaraId}/stats")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<TicketStatsRes> getTicketStatsByAcara(@PathVariable Long acaraId) {
        return ResponseEntity.ok(tiketService.getTicketStatsByAcara(acaraId));
    }

    @GetMapping("/acara/{acaraId}/manifest")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<List<ManifestItemRes>> getManifestByAcara(@PathVariable Long acaraId) {
        return ResponseEntity.ok(tiketService.getManifestByAcara(acaraId));
    }
}

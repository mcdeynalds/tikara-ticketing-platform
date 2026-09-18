package com.tiketkonser.event_service.controller;

import com.tiketkonser.event_service.payload.req.AcaraReq;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.OrganizerAnalyticsRes;
import com.tiketkonser.event_service.payload.res.OrganizerEventDetailRes;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.service.AcaraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping({"/api/acara", "/api/events"})
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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String kota,
            @RequestParam(required = false) Long kategoriId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "tanggalMulai") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        String searchTerm = (keyword != null && !keyword.trim().isEmpty()) ? keyword : search;
        String categoryName = (category != null && !category.trim().isEmpty()) ? category : genre;
        return ResponseEntity.ok(acaraService.getAll(searchTerm, kota, kategoriId, categoryName, status, page, size, sortBy, sortDir, authentication));
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<PageResponse<AcaraRes>> getMyEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        return ResponseEntity.ok(acaraService.getMyEvents(search, status, page, size, sortBy, sortDir, authentication));
    }

    @GetMapping("/organizer/analytics")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<OrganizerAnalyticsRes> getOrganizerAnalytics(Authentication authentication) {
        return ResponseEntity.ok(acaraService.getOrganizerAnalytics(authentication));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<AcaraRes> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String newStatus = body != null ? body.get("status") : "DRAFT";
        return ResponseEntity.ok(acaraService.changeStatus(id, newStatus, authentication));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<AcaraRes> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(acaraService.getById(id, authentication));
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

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<AcaraRes> restore(@PathVariable Long id) {
        return ResponseEntity.ok(acaraService.restore(id));
    }

    @PostMapping(value = "/{id}/upload-poster", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<AcaraRes> uploadPoster(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(acaraService.uploadPoster(id, file));
    }

    @PostMapping(value = "/{id}/upload-flyer", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<AcaraRes> uploadFlyerPdf(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(acaraService.uploadFlyerPdf(id, file));
    }

    @GetMapping("/pending-review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AcaraRes>> getPendingReview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(acaraService.getPendingReview(page, size, authentication));
    }

    @PatchMapping("/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AcaraRes> moderateEvent(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String action = body != null ? body.get("action") : "APPROVE";
        String catatan = body != null ? body.get("catatan") : "";
        return ResponseEntity.ok(acaraService.moderateEvent(id, action, catatan, authentication));
    }

    @PatchMapping("/{id}/resubmit")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<AcaraRes> resubmitEvent(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(acaraService.resubmitEvent(id, authentication));
    }

    @GetMapping("/{id}/detail-organizer")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<OrganizerEventDetailRes> getEventDetailForOrganizer(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok(acaraService.getEventDetailForOrganizer(id, authentication, authHeader));
    }
}

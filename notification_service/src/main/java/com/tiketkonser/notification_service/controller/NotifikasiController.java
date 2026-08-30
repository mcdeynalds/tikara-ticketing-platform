package com.tiketkonser.notification_service.controller;

import com.tiketkonser.notification_service.payload.req.SendNotifikasiReq;
import com.tiketkonser.notification_service.payload.res.NotifikasiRes;
import com.tiketkonser.notification_service.payload.res.PageResponse;
import com.tiketkonser.notification_service.service.NotifikasiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotifikasiController {

    private final NotifikasiService notifikasiService;

    @PostMapping("/send")
    public ResponseEntity<NotifikasiRes> sendNotification(@Valid @RequestBody SendNotifikasiReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notifikasiService.sendNotification(request));
    }

    @GetMapping("/saya")
    public ResponseEntity<PageResponse<NotifikasiRes>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notifikasiService.getMyNotifications(authentication, page, size));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotifikasiRes> markAsRead(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(notifikasiService.markAsRead(id, authentication));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(notifikasiService.getUnreadCount(authentication));
    }
}
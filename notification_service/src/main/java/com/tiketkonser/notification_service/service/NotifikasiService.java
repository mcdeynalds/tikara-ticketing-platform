package com.tiketkonser.notification_service.service;

import com.tiketkonser.notification_service.payload.req.SendNotifikasiReq;
import com.tiketkonser.notification_service.payload.res.NotifikasiRes;
import com.tiketkonser.notification_service.payload.res.PageResponse;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface NotifikasiService {
    NotifikasiRes sendNotification(SendNotifikasiReq request);
    PageResponse<NotifikasiRes> getMyNotifications(Authentication authentication, int page, int size);
    NotifikasiRes markAsRead(Long id, Authentication authentication);
    Map<String, Object> getUnreadCount(Authentication authentication);
}
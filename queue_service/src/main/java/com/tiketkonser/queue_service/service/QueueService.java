package com.tiketkonser.queue_service.service;

import com.tiketkonser.queue_service.payload.req.JoinQueueReq;
import com.tiketkonser.queue_service.payload.res.QueueStatusRes;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface QueueService {
    QueueStatusRes joinQueue(JoinQueueReq request, Authentication authentication);
    QueueStatusRes getQueueStatus(Long acaraId, Authentication authentication);
    List<QueueStatusRes> processNextBatch(Long acaraId, int batchSize, Authentication authentication);
}
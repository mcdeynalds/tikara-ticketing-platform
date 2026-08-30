package com.tiketkonser.queue_service.controller;

import com.tiketkonser.queue_service.payload.req.JoinQueueReq;
import com.tiketkonser.queue_service.payload.res.QueueStatusRes;
import com.tiketkonser.queue_service.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/join")
    public ResponseEntity<QueueStatusRes> joinQueue(@Valid @RequestBody JoinQueueReq request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(queueService.joinQueue(request, authentication));
    }

    @GetMapping("/status")
    public ResponseEntity<QueueStatusRes> getQueueStatus(@RequestParam Long acaraId, Authentication authentication) {
        return ResponseEntity.ok(queueService.getQueueStatus(acaraId, authentication));
    }

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('PENYELENGGARA', 'ADMIN')")
    public ResponseEntity<List<QueueStatusRes>> processNextBatch(
            @RequestParam Long acaraId,
            @RequestParam(defaultValue = "10") int batchSize,
            Authentication authentication) {

        return ResponseEntity.ok(queueService.processNextBatch(acaraId, batchSize, authentication));
    }
}
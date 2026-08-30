package com.tiketkonser.ticket_service.service.impl;

import com.tiketkonser.ticket_service.entity.KategoriTiket;
import com.tiketkonser.ticket_service.entity.Tiket;
import com.tiketkonser.ticket_service.handler.BadRequestException;
import com.tiketkonser.ticket_service.handler.NotFoundException;
import com.tiketkonser.ticket_service.payload.req.IssueTiketReq;
import com.tiketkonser.ticket_service.payload.res.PageResponse;
import com.tiketkonser.ticket_service.payload.res.TiketRes;
import com.tiketkonser.ticket_service.repository.KategoriTiketRepository;
import com.tiketkonser.ticket_service.repository.TiketRepository;
import com.tiketkonser.ticket_service.service.TiketService;
import com.tiketkonser.ticket_service.utility.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TiketServiceImpl implements TiketService {

    private final TiketRepository tiketRepository;
    private final KategoriTiketRepository kategoriTiketRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public TiketRes issueTiket(IssueTiketReq request) {
        KategoriTiket kt = kategoriTiketRepository.findById(request.getKategoriTiketId())
                .orElseThrow(() -> new NotFoundException("Kategori tiket tidak ditemukan"));

        String uniqueQr = "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis() % 10000;

        Tiket tiket = Tiket.builder()
                .kategoriTiket(kt)
                .detailPemesananId(request.getDetailPemesananId())
                .penggunaId(request.getPenggunaId())
                .kodeQr(uniqueQr)
                .status("ISSUED")
                .diterbitkanAt(LocalDateTime.now())
                .build();

        return ticketMapper.toTiketRes(tiketRepository.save(tiket));
    }

    @Override
    public PageResponse<TiketRes> getMyTiket(Authentication authentication, int page, int size) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        Pageable pageable = PageRequest.of(page, size, Sort.by("diterbitkanAt").descending());
        Page<Tiket> tiketPage = tiketRepository.findByPenggunaIdAndDeletedAtIsNull(userId, pageable);

        List<TiketRes> content = tiketPage.getContent().stream()
                .map(ticketMapper::toTiketRes)
                .collect(Collectors.toList());

        return PageResponse.<TiketRes>builder()
                .content(content)
                .pageNumber(tiketPage.getNumber())
                .pageSize(tiketPage.getSize())
                .totalElements(tiketPage.getTotalElements())
                .totalPages(tiketPage.getTotalPages())
                .isLast(tiketPage.isLast())
                .build();
    }

    @Override
    public TiketRes getByKodeQr(String kodeQr) {
        Tiket tiket = tiketRepository.findByKodeQrAndDeletedAtIsNull(kodeQr)
                .orElseThrow(() -> new NotFoundException("Tiket tidak ditemukan"));
        return ticketMapper.toTiketRes(tiket);
    }

    @Override
    @Transactional
    public TiketRes validateOrUseTiket(String kodeQr) {
        Tiket tiket = tiketRepository.findByKodeQrAndDeletedAtIsNull(kodeQr)
                .orElseThrow(() -> new NotFoundException("Tiket tidak ditemukan"));

        if ("USED".equalsIgnoreCase(tiket.getStatus())) {
            throw new BadRequestException("Tiket ini sudah pernah digunakan untuk check-in");
        }
        if ("CANCELLED".equalsIgnoreCase(tiket.getStatus())) {
            throw new BadRequestException("Tiket ini telah dibatalkan");
        }

        tiket.setStatus("USED");
        tiket.setDigunakanAt(LocalDateTime.now());

        return ticketMapper.toTiketRes(tiketRepository.save(tiket));
    }
}
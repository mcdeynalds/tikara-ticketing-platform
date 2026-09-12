package com.tiketkonser.event_service.service.impl;

import com.tiketkonser.event_service.entity.Acara;
import com.tiketkonser.event_service.entity.Ulasan;
import com.tiketkonser.event_service.handler.NotFoundException;
import com.tiketkonser.event_service.payload.req.UlasanReq;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.payload.res.UlasanRes;
import com.tiketkonser.event_service.repository.AcaraRepository;
import com.tiketkonser.event_service.repository.UlasanRepository;
import com.tiketkonser.event_service.service.UlasanService;
import com.tiketkonser.event_service.utility.EventMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UlasanServiceImpl implements UlasanService {

    private final UlasanRepository ulasanRepository;
    private final AcaraRepository acaraRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public UlasanRes create(Long acaraId, UlasanReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(acaraId)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        Long userId = (Long) authentication.getCredentials();
        String userEmail = authentication.getName();

        Ulasan ulasan = Ulasan.builder()
                .acara(acara)
                .penggunaId(userId != null ? userId : 1L)
                .penggunaNama(userEmail)
                .rating(request.getRating())
                .komentar(request.getKomentar())
                .createdAt(LocalDateTime.now())
                .build();

        return eventMapper.toUlasanRes(ulasanRepository.save(ulasan));
    }

    @Override
    public PageResponse<UlasanRes> getByAcaraId(Long acaraId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Ulasan> ulasanPage = ulasanRepository.findByAcaraId(acaraId, pageable);

        List<UlasanRes> content = ulasanPage.getContent().stream()
                .map(eventMapper::toUlasanRes)
                .collect(Collectors.toList());

        return PageResponse.<UlasanRes>builder()
                .content(content)
                .pageNumber(ulasanPage.getNumber())
                .pageSize(ulasanPage.getSize())
                .totalElements(ulasanPage.getTotalElements())
                .totalPages(ulasanPage.getTotalPages())
                .isLast(ulasanPage.isLast())
                .build();
    }
}

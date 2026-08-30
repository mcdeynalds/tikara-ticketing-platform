package com.tiketkonser.event_service.service.impl;

import com.tiketkonser.event_service.entity.Acara;
import com.tiketkonser.event_service.entity.Kategori;
import com.tiketkonser.event_service.handler.BadRequestException;
import com.tiketkonser.event_service.handler.NotFoundException;
import com.tiketkonser.event_service.payload.req.AcaraReq;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.repository.AcaraRepository;
import com.tiketkonser.event_service.repository.KategoriRepository;
import com.tiketkonser.event_service.service.AcaraService;
import com.tiketkonser.event_service.utility.AcaraSpecification;
import com.tiketkonser.event_service.utility.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcaraServiceImpl implements AcaraService {

    private final AcaraRepository acaraRepository;
    private final KategoriRepository kategoriRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public AcaraRes create(AcaraReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long organizerId = (Long) authentication.getCredentials();
        String organizerEmail = authentication.getName();

        Set<Kategori> meKategori = new HashSet<>();
        if (request.getKategoriIds() != null && !request.getKategoriIds().isEmpty()) {
            meKategori = new HashSet<>(kategoriRepository.findAllById(request.getKategoriIds()));
        }

        Acara acara = Acara.builder()
                .organizerId(organizerId != null ? organizerId : 1L)
                .organizerNama(organizerEmail)
                .nama(request.getNama())
                .deskripsi(request.getDeskripsi())
                .venue(request.getVenue())
                .kota(request.getKota())
                .tanggalMulai(request.getTanggalMulai())
                .tanggalSelesai(request.getTanggalSelesai())
                .status("PUBLISHED") // Default PUBLISHED untuk kemudahan
                .gambarUrl(request.getGambarUrl())
                .meKategori(meKategori)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return eventMapper.toAcaraRes(acaraRepository.save(acara));
    }

    @Override
    public AcaraRes getById(Long id) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));
        return eventMapper.toAcaraRes(acara);
    }

    @Override
    public PageResponse<AcaraRes> getAll(String search, String kota, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Acara> spec = AcaraSpecification.filterAcara(search, kota, status);

        Page<Acara> pageData = acaraRepository.findAll(spec, pageable);
        List<AcaraRes> content = pageData.getContent().stream().map(eventMapper::toAcaraRes).collect(Collectors.toList());

        return PageResponse.<AcaraRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    @Override
    @Transactional
    public AcaraRes update(Long id, AcaraReq request, Authentication authentication) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        validateOrganizerOrAdmin(acara.getOrganizerId(), authentication);

        acara.setNama(request.getNama());
        acara.setDeskripsi(request.getDeskripsi());
        acara.setVenue(request.getVenue());
        acara.setKota(request.getKota());
        acara.setTanggalMulai(request.getTanggalMulai());
        acara.setTanggalSelesai(request.getTanggalSelesai());
        if (request.getGambarUrl() != null) acara.setGambarUrl(request.getGambarUrl());

        if (request.getKategoriIds() != null) {
            Set<Kategori> meKategori = new HashSet<>(kategoriRepository.findAllById(request.getKategoriIds()));
            acara.setMeKategori(meKategori);
        }

        acara.setUpdatedAt(LocalDateTime.now());
        return eventMapper.toAcaraRes(acaraRepository.save(acara));
    }

    @Override
    @Transactional
    public void delete(Long id, Authentication authentication) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        validateOrganizerOrAdmin(acara.getOrganizerId(), authentication);

        acara.setDeletedAt(LocalDateTime.now());
        acara.setUpdatedAt(LocalDateTime.now());
        acaraRepository.save(acara);
    }

    @Override
    @Transactional
    public AcaraRes moderate(Long id, String status, Authentication authentication) {
        validateAdmin(authentication);

        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        acara.setStatus(status.toUpperCase());
        acara.setUpdatedAt(LocalDateTime.now());
        return eventMapper.toAcaraRes(acaraRepository.save(acara));
    }

    private void validateOrganizerOrAdmin(Long organizerId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return;

        Long currentUserId = (Long) authentication.getCredentials();
        if (currentUserId != null && !currentUserId.equals(organizerId)) {
            throw new AccessDeniedException("Anda tidak memiliki hak akses untuk acara ini");
        }
    }

    private void validateAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Hanya ADMIN yang memiliki hak akses untuk fungsi ini");
        }
    }
}
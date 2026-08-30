package com.tiketkonser.event_service.service.impl;

import com.tiketkonser.event_service.entity.Kategori;
import com.tiketkonser.event_service.handler.ConflictException;
import com.tiketkonser.event_service.handler.NotFoundException;
import com.tiketkonser.event_service.payload.req.KategoriReq;
import com.tiketkonser.event_service.payload.res.KategoriRes;
import com.tiketkonser.event_service.repository.KategoriRepository;
import com.tiketkonser.event_service.service.KategoriService;
import com.tiketkonser.event_service.utility.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KategoriServiceImpl implements KategoriService {

    private final KategoriRepository kategoriRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public KategoriRes create(KategoriReq request) {
        if (kategoriRepository.existsByNamaKategoriIgnoreCase(request.getNamaKategori())) {
            throw new ConflictException("Nama kategori sudah ada");
        }

        Kategori kategori = Kategori.builder()
                .namaKategori(request.getNamaKategori())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return eventMapper.toKategoriRes(kategoriRepository.save(kategori));
    }

    @Override
    public List<KategoriRes> getAll() {
        return kategoriRepository.findAll().stream()
                .map(eventMapper::toKategoriRes)
                .collect(Collectors.toList());
    }

    @Override
    public KategoriRes getById(Long id) {
        Kategori kategori = kategoriRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kategori tidak ditemukan"));
        return eventMapper.toKategoriRes(kategori);
    }
}
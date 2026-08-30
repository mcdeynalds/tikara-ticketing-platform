package com.tiketkonser.ticket_service.service.impl;

import com.tiketkonser.ticket_service.entity.KategoriTiket;
import com.tiketkonser.ticket_service.handler.BadRequestException;
import com.tiketkonser.ticket_service.handler.NotFoundException;
import com.tiketkonser.ticket_service.payload.req.HoldTiketReq;
import com.tiketkonser.ticket_service.payload.req.KategoriTiketReq;
import com.tiketkonser.ticket_service.payload.res.KategoriTiketRes;
import com.tiketkonser.ticket_service.repository.KategoriTiketRepository;
import com.tiketkonser.ticket_service.service.KategoriTiketService;
import com.tiketkonser.ticket_service.utility.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KategoriTiketServiceImpl implements KategoriTiketService {

    private final KategoriTiketRepository kategoriTiketRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public KategoriTiketRes create(KategoriTiketReq request) {
        KategoriTiket kt = KategoriTiket.builder()
                .acaraId(request.getAcaraId())
                .namaKategori(request.getNamaKategori())
                .harga(request.getHarga())
                .kuota(request.getKuota())
                .terjual(0)
                .direservasi(0)
                .mulaiJual(request.getMulaiJual())
                .selesaiJual(request.getSelesaiJual())
                .status("AVAILABLE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return ticketMapper.toKategoriTiketRes(kategoriTiketRepository.save(kt));
    }

    @Override
    public List<KategoriTiketRes> getByAcaraId(Long acaraId) {
        return kategoriTiketRepository.findByAcaraId(acaraId).stream()
                .map(ticketMapper::toKategoriTiketRes)
                .collect(Collectors.toList());
    }

    @Override
    public KategoriTiketRes getById(Long id) {
        KategoriTiket kt = kategoriTiketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kategori tiket tidak ditemukan"));
        return ticketMapper.toKategoriTiketRes(kt);
    }

    @Override
    @Transactional
    public KategoriTiketRes update(Long id, KategoriTiketReq request) {
        KategoriTiket kt = kategoriTiketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kategori tiket tidak ditemukan"));

        kt.setNamaKategori(request.getNamaKategori());
        kt.setHarga(request.getHarga());
        kt.setKuota(request.getKuota());
        kt.setMulaiJual(request.getMulaiJual());
        kt.setSelesaiJual(request.getSelesaiJual());
        kt.setUpdatedAt(LocalDateTime.now());

        return ticketMapper.toKategoriTiketRes(kategoriTiketRepository.save(kt));
    }

    @Override
    @Transactional
    public void holdStok(HoldTiketReq request) {
        KategoriTiket kt = kategoriTiketRepository.findById(request.getKategoriTiketId())
                .orElseThrow(() -> new NotFoundException("Kategori tiket tidak ditemukan"));

        int sisa = kt.getKuota() - kt.getTerjual() - kt.getDireservasi();
        if (sisa < request.getJumlah()) {
            throw new BadRequestException("Stok tiket tidak mencukupi untuk reservasi");
        }

        kt.setDireservasi(kt.getDireservasi() + request.getJumlah());
        kt.setUpdatedAt(LocalDateTime.now());
        kategoriTiketRepository.save(kt);
    }

    @Override
    @Transactional
    public void releaseStok(HoldTiketReq request) {
        KategoriTiket kt = kategoriTiketRepository.findById(request.getKategoriTiketId())
                .orElseThrow(() -> new NotFoundException("Kategori tiket tidak ditemukan"));

        int currentHold = kt.getDireservasi();
        int releaseQty = Math.min(currentHold, request.getJumlah());

        kt.setDireservasi(currentHold - releaseQty);
        kt.setUpdatedAt(LocalDateTime.now());
        kategoriTiketRepository.save(kt);
    }

    @Override
    @Transactional
    public void confirmStok(HoldTiketReq request) {
        KategoriTiket kt = kategoriTiketRepository.findById(request.getKategoriTiketId())
                .orElseThrow(() -> new NotFoundException("Kategori tiket tidak ditemukan"));

        int currentHold = kt.getDireservasi();
        int qty = request.getJumlah();

        if (currentHold >= qty) {
            kt.setDireservasi(currentHold - qty);
        } else {
            kt.setDireservasi(0);
        }

        kt.setTerjual(kt.getTerjual() + qty);
        if (kt.getTerjual() >= kt.getKuota()) {
            kt.setStatus("SOLD_OUT");
        }
        kt.setUpdatedAt(LocalDateTime.now());
        kategoriTiketRepository.save(kt);
    }
}
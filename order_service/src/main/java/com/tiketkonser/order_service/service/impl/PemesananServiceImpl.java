package com.tiketkonser.order_service.service.impl;

import com.tiketkonser.order_service.entity.DetailPemesanan;
import com.tiketkonser.order_service.entity.Pemesanan;
import com.tiketkonser.order_service.handler.BadRequestException;
import com.tiketkonser.order_service.handler.NotFoundException;
import com.tiketkonser.order_service.payload.req.DetailPemesananItemReq;
import com.tiketkonser.order_service.payload.req.PemesananReq;
import com.tiketkonser.order_service.payload.res.PageResponse;
import com.tiketkonser.order_service.payload.res.PemesananRes;
import com.tiketkonser.order_service.repository.PemesananRepository;
import com.tiketkonser.order_service.service.PemesananService;
import com.tiketkonser.order_service.utility.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PemesananServiceImpl implements PemesananService {

    private final PemesananRepository pemesananRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public PemesananRes createOrder(PemesananReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        String nomorPesanan = "ORD-" + System.currentTimeMillis() / 1000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        BigDecimal totalHarga = BigDecimal.ZERO;
        List<DetailPemesanan> items = new ArrayList<>();

        Pemesanan pemesanan = Pemesanan.builder()
                .penggunaId(userId)
                .nomorPesanan(nomorPesanan)
                .totalHarga(BigDecimal.ZERO)
                .status("PENDING")
                .tenggatPembayaran(LocalDateTime.now().plusMinutes(15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        for (DetailPemesananItemReq itemReq : request.getItems()) {
            BigDecimal subtotal = itemReq.getHargaSatuan().multiply(BigDecimal.valueOf(itemReq.getJumlah()));
            totalHarga = totalHarga.add(subtotal);

            DetailPemesanan detail = DetailPemesanan.builder()
                    .pemesanan(pemesanan)
                    .kategoriTiketId(itemReq.getKategoriTiketId())
                    .jumlah(itemReq.getJumlah())
                    .hargaSatuan(itemReq.getHargaSatuan())
                    .subtotal(subtotal)
                    .build();
            items.add(detail);
        }

        pemesanan.setTotalHarga(totalHarga);
        pemesanan.setDetailPemesananList(items);

        Pemesanan savedOrder = pemesananRepository.save(pemesanan);
        return orderMapper.toPemesananRes(savedOrder);
    }

    @Override
    public PageResponse<PemesananRes> getMyOrders(Authentication authentication, int page, int size) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pemesanan> orderPage = pemesananRepository.findByPenggunaIdAndDeletedAtIsNull(userId, pageable);

        List<PemesananRes> content = orderPage.getContent().stream()
                .map(orderMapper::toPemesananRes)
                .collect(Collectors.toList());

        return PageResponse.<PemesananRes>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isLast(orderPage.isLast())
                .build();
    }

    @Override
    public PemesananRes getOrderById(Long id, Authentication authentication) {
        Pemesanan order = pemesananRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pesanan tidak ditemukan"));

        validateOwnershipOrAdmin(order.getPenggunaId(), authentication);
        return orderMapper.toPemesananRes(order);
    }

    @Override
    @Transactional
    public PemesananRes cancelOrder(Long id, Authentication authentication) {
        Pemesanan order = pemesananRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pesanan tidak ditemukan"));

        validateOwnershipOrAdmin(order.getPenggunaId(), authentication);

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Hanya pesanan berstatus PENDING yang dapat dibatalkan");
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toPemesananRes(pemesananRepository.save(order));
    }

    @Override
    @Transactional
    public PemesananRes paySimulation(Long id, Authentication authentication) {
        Pemesanan order = pemesananRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pesanan tidak ditemukan"));

        validateOwnershipOrAdmin(order.getPenggunaId(), authentication);

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Pesanan tidak berstatus PENDING untuk dibayar");
        }

        order.setStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toPemesananRes(pemesananRepository.save(order));
    }

    private void validateOwnershipOrAdmin(Long targetUserId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return;

        Long currentUserId = (Long) authentication.getCredentials();
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Anda tidak memiliki hak akses untuk pesanan ini");
        }
    }
}
package com.tiketkonser.order_service.payload.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PemesananReq {

    @NotEmpty(message = "Daftar item tiket tidak boleh kosong")
    @Valid
    private List<DetailPemesananItemReq> items;
}
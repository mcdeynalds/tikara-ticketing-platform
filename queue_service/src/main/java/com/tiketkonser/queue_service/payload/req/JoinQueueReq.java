package com.tiketkonser.queue_service.payload.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinQueueReq {

    @NotNull(message = "Acara ID wajib diisi")
    private Long acaraId;
}
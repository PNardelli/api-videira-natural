package com.api.dto.requests;

import java.math.BigDecimal;

public record PromocaoRequest(
        Boolean emPromocao,
        BigDecimal valorPromocional
) {}

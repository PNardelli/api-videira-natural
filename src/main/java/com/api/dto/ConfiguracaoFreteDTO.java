package com.api.dto;

import com.api.entity.FaixaFrete;

import java.util.List;

public record ConfiguracaoFreteDTO(
        Boolean ativo,
        String enderecoOrigem,
        String bairroOrigem,
        String cepOrigem,
        String cidadeOrigem,
        String estadoOrigem,
        List<FaixaFrete> faixaFretes
) {}

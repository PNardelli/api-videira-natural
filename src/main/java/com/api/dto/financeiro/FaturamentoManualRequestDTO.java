package com.api.dto.financeiro;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FaturamentoManualRequestDTO {
    private Integer ano;
    private Integer mes;
    private BigDecimal valor;
    private Long quantidadePedidos;
    private LocalDate dataReferencia;
}

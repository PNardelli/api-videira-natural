package com.api.dto.financeiro;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MensalidadeRequestDTO {
    private Integer ano;
    private Integer mes;
    private BigDecimal valor;
    private Boolean pago;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private LocalDate dataReferencia;
}

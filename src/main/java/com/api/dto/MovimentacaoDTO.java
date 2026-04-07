package com.api.dto;

import com.api.eNum.TipoMovimentacao;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovimentacaoDTO {
    private Long produtoId;
    private Long fornecedorId;
    private BigDecimal quantidade;
    private TipoMovimentacao tipo;
    private BigDecimal precoVenda;
    private BigDecimal precoCompra;
    private String unidadeMedida;
    private String observacao;
    private LocalDate dataValidade;
    private Boolean isGramas;
}

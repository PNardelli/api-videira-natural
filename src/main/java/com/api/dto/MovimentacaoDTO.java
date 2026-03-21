package com.api.dto;

import com.api.eNum.TipoMovimentacao;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovimentacaoDTO {
    private Long produtoId;
    private Long fornecedorId;
    private Integer quantidade;
    private TipoMovimentacao tipo;
    private String observacao;
    private LocalDate dataValidade;
}

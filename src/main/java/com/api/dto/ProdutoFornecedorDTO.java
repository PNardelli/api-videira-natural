package com.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProdutoFornecedorDTO {

    private Long produtoId;
    private Long fornecedorId;
    private LocalDateTime dataEntrada;
    private LocalDate dataValidade;
    private String unidade;
    private Integer quantidade;
    private BigDecimal precoVenda;
    private BigDecimal precoCompra;

}

package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalancaProdutoDTO {
    private String codigoProduto;
    private String nome;
    private LocalDate dataValidade;
    private BigDecimal preco;
    private String dataValidadeDias;
}
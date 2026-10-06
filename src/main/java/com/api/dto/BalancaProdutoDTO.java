package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalancaProdutoDTO {
    private String codigoProduto;
    private String nome;
    private LocalDate dataValidade;
    private BigDecimal preco; // Se precisar para a balança
}
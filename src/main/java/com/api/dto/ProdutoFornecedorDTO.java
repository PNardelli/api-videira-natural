package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProdutoFornecedorDTO {

    private Long produtoId;
    private String codigoProduto;
    private String nome;
    private Long fornecedorId;
    private LocalDateTime dataEntrada;
    private LocalDate dataValidade;
    private String dataValidadeDias;
    private String unidadeMedida;
    private BigDecimal quantidade;
    private BigDecimal precoVenda;
    private BigDecimal precoCompra;
    private BigDecimal estoqueMinimo;

    public ProdutoFornecedorDTO(String codigoProduto, String nome, Long produtoId, BigDecimal quantidade, LocalDate dataValidade, BigDecimal precoVenda, BigDecimal estoqueMinimo, String unidadeMedida) {
        this.codigoProduto = codigoProduto;
        this.nome = nome;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.dataValidade = dataValidade;
        this.precoVenda = precoVenda;
        this.estoqueMinimo = estoqueMinimo;
        this.unidadeMedida = unidadeMedida;
    }


}

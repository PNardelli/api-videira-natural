package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EstoqueItemDTO {
    private Long idVinculo;
    private Long produtoId;
    private String codigoProduto;
    private String nome;
    private String fornecedorNome;
    private BigDecimal quantidade;      // Ajustado para BigDecimal (conforme o tipo nativo da tabela pf.quantidade)
    private BigDecimal estoqueMinimo;   // Ajustado para BigDecimal
    private String unidadeMedida;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private LocalDate dataValidade;

    // CONSTRUTOR NA ORDEM EXATA DA QUERY
    public EstoqueItemDTO(Long idVinculo, Long produtoId, String codigoProduto, String nome,
                          String fornecedorNome, BigDecimal quantidade, BigDecimal estoqueMinimo,
                          String unidadeMedida, BigDecimal precoCompra, BigDecimal precoVenda, LocalDate dataValidade) {
        this.idVinculo = idVinculo;
        this.produtoId = produtoId;
        this.codigoProduto = codigoProduto;
        this.nome = nome;
        this.fornecedorNome = fornecedorNome != null ? fornecedorNome : "NÃO CADASTRADO";
        this.quantidade = quantidade != null ? quantidade : BigDecimal.ZERO;
        this.estoqueMinimo = estoqueMinimo != null ? estoqueMinimo : new BigDecimal("5");
        this.unidadeMedida = unidadeMedida;
        this.precoCompra = precoCompra;
        this.precoVenda = precoVenda;
        this.dataValidade = dataValidade;
    }

}

package com.api.dto;

import com.api.entity.Produto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
public class ProdutoComparativoDto {
    private Long id;
    private String nome;
    private String codigoBarra;
    private BigDecimal quantidadeNota;
    private BigDecimal estoqueAtual;
    private BigDecimal novoEstoque;

    public ProdutoComparativoDto(Produto p, BigDecimal qtdNota) {
        this.id = p.getId();
        this.nome = p.getNome();
        this.codigoBarra = p.getCodigoBarras();
        this.quantidadeNota = qtdNota;
        this.estoqueAtual = p.getEstoque() != null ? p.getEstoque() : BigDecimal.valueOf(0);
        //this.novoEstoque = this.estoqueAtual + qtdNota;
    }
}

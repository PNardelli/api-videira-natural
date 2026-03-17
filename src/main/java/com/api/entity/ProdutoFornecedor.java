package com.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ProdutoFornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    @JsonIgnoreProperties("fornecedores")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    @JsonIgnoreProperties("produtosVinculados") // Não deixa o fornecedor carregar a lista de produtos dele aqui
    private Fornecedor fornecedor;

    private BigDecimal precoCompra;
    private String dataValidade; // O segredo está aqui
    private String unidade;
    private Integer quantidade;
}

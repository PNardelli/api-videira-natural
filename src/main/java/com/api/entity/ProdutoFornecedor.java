package com.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tb_produto_fornecedor")
public class ProdutoFornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    private LocalDateTime dataEntrada;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private LocalDate dataValidade;
    private String unidade;
    private BigDecimal quantidade;
    private Boolean produtoAtivo;
}

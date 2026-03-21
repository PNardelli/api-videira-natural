package com.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ProdutoFornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne // <--- A CORREÇÃO
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    private LocalDateTime dataEntrada;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private String dataValidade;
    private String unidade;
    private Integer quantidade;
}

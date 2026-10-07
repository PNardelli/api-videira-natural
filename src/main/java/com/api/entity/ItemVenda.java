package com.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "tb_item_venda")
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    @JsonIgnore
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "produto_fornecedor_id")
    private ProdutoFornecedor produtoFornecedor;

    private BigDecimal quantidade;

    private BigDecimal precoUnitario;

    // Essencial para relatórios de margem/lucro precisos (guarda o custo da época da venda)
    private BigDecimal precoCustoUnitario;

    private BigDecimal subtotal;
}
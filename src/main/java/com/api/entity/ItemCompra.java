package com.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "tb_itens_compra")
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "produto_fornecedor_id")
    private ProdutoFornecedor produtoFornecedor;

    private Long produtoId;

    private BigDecimal quantidade;
    private Double valorUnitario;
    private Double valorVenda;

}

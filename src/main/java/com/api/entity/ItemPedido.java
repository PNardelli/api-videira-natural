package com.api.entity;

import com.api.eNum.StatusItemPedido;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "tb_item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore
    private Pedido pedido;

    // Referência direta ao lote/fornecedor de onde o estoque será abatido
    @ManyToOne
    @JoinColumn(name = "produto_fornecedor_id", nullable = false)
    private ProdutoFornecedor produtoFornecedor;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "nome_produto")
    private String nomeProduto;

    @Column(nullable = false)
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "status_reserva")
    @Enumerated(EnumType.STRING)
    private StatusItemPedido statusReserva;


    public ItemPedido(Long id, @NotNull String nome, BigDecimal quantidade, BigDecimal precoUnitario) {
    }

    public ItemPedido() {

    }
}
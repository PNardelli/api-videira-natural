package com.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PedidoRequest {

    private Long clienteId;
    private List<ItemPedidoRequest> itens;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private String formaPagamento;
}


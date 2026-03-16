package com.api.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PedidoRequest {

    private Long clienteId;
    private List<ItemPedidoRequest> itens;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private String formaPagamento;
}


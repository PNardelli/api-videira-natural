package com.api.dto.requests;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PedidoRequest {

    private Long clienteId;
    private List<ItemCarrinhoRequest> itens;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private String formaPagamento;
}


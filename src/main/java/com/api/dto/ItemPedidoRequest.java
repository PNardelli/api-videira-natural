package com.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemPedidoRequest {

        private Long produtoId;
        private BigDecimal quantidade;
}

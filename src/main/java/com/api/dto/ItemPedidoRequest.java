package com.api.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemPedidoRequest {

        private Long produtoId;
        private BigDecimal quantidade;
}

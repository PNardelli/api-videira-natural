package com.api.dto.requests;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemCarrinhoRequest {

        private Long produtoId;
        private BigDecimal quantidade;
        private String nome;
        private BigDecimal preco;
        private BigDecimal subTotal;
        private String unidade;
}

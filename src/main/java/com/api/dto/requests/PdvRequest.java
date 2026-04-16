package com.api.dto.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PdvRequest {

    private String termo; // O que foi bipado ou digitado
    private Long produtoId; // O campo a mais que você quer
    private List<ItemCarrinhoRequest> carrinho;
}

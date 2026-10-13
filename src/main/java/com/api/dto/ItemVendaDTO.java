package com.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemVendaDTO {
    private String nome;
    private Long produtoId;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario; // Importante enviar para garantir o preço do momento do bipe
}

package com.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemKitDTO {
    private Long produtoId;
    private Long produtoFormecedorId;
    private String nomeProduto;
    private BigDecimal quantidade;
}

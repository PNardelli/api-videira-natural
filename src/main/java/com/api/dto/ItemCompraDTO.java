package com.api.dto;

import lombok.Data;

@Data
public class ItemCompraDTO {

    private Long produtoId;
    private Integer quantidade;
    private Double valorUnitario;
}

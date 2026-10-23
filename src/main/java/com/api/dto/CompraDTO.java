package com.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompraDTO {
    private Long fornecedorId;
    private List<ItemCompraDTO> itens;
}
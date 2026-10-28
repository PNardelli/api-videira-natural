package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCompraDTO {
    private Long id;              // Essencial para identificar o item na edição
    private Long produtoId;
    private String nomeProduto;
    private String unidadeMedida;
    private BigDecimal quantidade;
    private Double valorUnitario;
    private Double valorOriginal;
    private Double valorVenda;// <-- Para comparar se houve alteração
}
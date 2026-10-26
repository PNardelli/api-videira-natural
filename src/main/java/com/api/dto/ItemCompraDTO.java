package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCompraDTO {
    private Long id;              // Essencial para identificar o item na edição
    private Long produtoId;
    private String nomeProduto;
    private String unidadeMedida;
    private Integer quantidade;
    private Double valorUnitario;
    private Double valorOriginal; // <-- Para comparar se houve alteração
}
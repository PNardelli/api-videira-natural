package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompraDetalheDTO {
    private Long id;
    private LocalDateTime dataHora;
    private String status;
    private LocalDate previsaoEntrega;
    private LocalDate dataEntrega;
    private Long fornecedorId;
    private String nomeFornecedor;
    private List<ItemCompraDTO> itens;
}
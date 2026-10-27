package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompraResumoDTO {
    private Long id;
    private LocalDateTime dataHora;
    private String nomeFornecedor;
    private Long totalItens;
    private String status;
    private LocalDate previsaoEntrega;
    private LocalDate dataEntrega;
    private String identificadorNota;
}
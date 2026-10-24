package com.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompraResumoDTO {
    private Long id;
    private LocalDateTime dataHora;
    private String nomeFornecedor;
    private int totalItens;
    private String status;
    private LocalDate previsaoEntrega;
    private LocalDate dataEntrega;

    public CompraResumoDTO(Long id, LocalDateTime dataHora, String nomeFornecedor, Long totalItens, String status, LocalDate previsaoEntrega, LocalDate dataEntrega) {
        this.id = id;
        this.dataHora = dataHora;
        this.nomeFornecedor = nomeFornecedor != null ? nomeFornecedor : "Não informado";
        this.totalItens = totalItens != null ? totalItens.intValue() : 0;
        this.status = status != null ? status : "PENDENTE";
        this.previsaoEntrega = previsaoEntrega;
        this.dataEntrega = dataEntrega;
    }
}
package com.api.dto.financeiro;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class FaturamentoAnoDTO {
    private Integer ano;
    private BigDecimal variacaoAnoPercentual;
    private List<FaturamentoMesDTO> meses;

    @Getter
    @Setter
    public static class FaturamentoMesDTO {
        private Integer mes;
        private BigDecimal faturamento;
        private Long pedidos;
        private BigDecimal ticketMedio;
        private String origem;
        private LocalDate dataReferencia;

    }
}

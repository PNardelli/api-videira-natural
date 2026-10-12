package com.api.dto;

import java.math.BigDecimal;

public class ResumoDTO {
    private BigDecimal totalFaturamento;
    private Long quantidadeVendas;
    private BigDecimal ticketMedio;

    public ResumoDTO(BigDecimal totalFaturamento, Long quantidadeVendas) {
        this.totalFaturamento = totalFaturamento != null ? totalFaturamento : BigDecimal.ZERO;
        this.quantidadeVendas = quantidadeVendas != null ? quantidadeVendas : 0L;
        if (this.quantidadeVendas > 0) {
            this.ticketMedio = this.totalFaturamento.divide(BigDecimal.valueOf(this.quantidadeVendas), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.ticketMedio = BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalFaturamento() { return totalFaturamento; }
    public Long getQuantidadeVendas() { return quantidadeVendas; }
    public BigDecimal getTicketMedio() { return ticketMedio; }
}

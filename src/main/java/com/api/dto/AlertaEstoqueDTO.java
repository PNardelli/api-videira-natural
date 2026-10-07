package com.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Essencial para o JPQL conseguir instanciar
public class AlertaEstoqueDTO {

    private String nomeProduto;
    private BigDecimal quantidade;
    private LocalDate dataValidade;

    // Você pode adicionar um método auxiliar para o Front-end
    public String getStatusVencimento() {
        if (dataValidade == null) return "Sem data";
        long dias = java.time.Duration.between(LocalDateTime.now(), dataValidade).toDays();
        if (dias < 0) return "Vencido";
        if (dias <= 7) return "Urgente";
        return "Próximo";
    }
}

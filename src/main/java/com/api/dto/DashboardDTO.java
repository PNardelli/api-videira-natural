package com.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DashboardDTO {
    // Cards de Resumo (Baseados na sua imagem)
    private Long totalClientes;
    private Long totalProdutos;
    private Long totalFornecedores;
    private BigDecimal faturamentoGeral;

    // Alertas de Estoque e Validade (Inteligência Videira Natural)
    private List<AlertaEstoqueDTO> produtosVencendo; // Próximos 30 dias
    private List<AlertaEstoqueDTO> estoqueBaixo;
    private List<AlertaEstoqueDTO> semEstoque;

    // Faturamento Periódico
    private BigDecimal faturamentoDiario;
    private BigDecimal faturamentoSemanal;
    private BigDecimal faturamentoMensal;
}

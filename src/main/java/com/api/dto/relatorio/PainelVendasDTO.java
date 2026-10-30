
package com.api.dto.relatorio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PainelVendasDTO {

    private PeriodoDTO periodo = new PeriodoDTO();
    private ResumoDTO resumo = new ResumoDTO();
    private ComparativoDTO comparativo = new ComparativoDTO();

    private List<GraficoDTO> grafico = new ArrayList<>();
    private List<FormaPagamentoDTO> formasPagamento = new ArrayList<>();
    private List<ProdutoMaisVendidoDTO> produtosMaisVendidos = new ArrayList<>();
    private List<VendaDTO> vendas = new ArrayList<>();

    // Período consultado
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodoDTO {
        private LocalDate inicio;
        private LocalDate fim;
        private String descricao;
    }

    // Indicadores principais
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumoDTO {
        private BigDecimal totalFaturamento = BigDecimal.ZERO;
        private Long quantidadeVendas = 0L;
        private Long quantidadeFinalizadas = 0L;
        private Long quantidadeCanceladas = 0L;
        private Long quantidadePendentes = 0L;
        private BigDecimal ticketMedio = BigDecimal.ZERO;
    }

    // Comparação com o período anterior
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparativoDTO {
        private BigDecimal faturamentoAnterior = BigDecimal.ZERO;
        private BigDecimal variacaoPercentual = BigDecimal.ZERO;
        private Long vendasAnterior = 0L;
    }

    // Dados para o gráfico de faturamento diário
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraficoDTO {
        private LocalDate data;
        private BigDecimal valor = BigDecimal.ZERO;
        private Long quantidadeVendas = 0L;
    }

    // Distribuição por forma de pagamento
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormaPagamentoDTO {
        private String formaPagamento;
        private BigDecimal valor = BigDecimal.ZERO;
        private BigDecimal percentual = BigDecimal.ZERO;
        private Long quantidade = 0L;
    }

    // Ranking de produtos vendidos
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoMaisVendidoDTO {
        private Long produtoId;
        private String nomeProduto;
        private BigDecimal quantidadeVendida = BigDecimal.ZERO;
        private BigDecimal faturamento = BigDecimal.ZERO;
    }

    // Histórico detalhado das vendas
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendaDTO {
        private Long id;
        private LocalDateTime dataVenda;
        private String clienteNome;
        private String status;
        private String canalVenda;

        private BigDecimal subtotal = BigDecimal.ZERO;
        private BigDecimal desconto = BigDecimal.ZERO;
        private BigDecimal frete = BigDecimal.ZERO;
        private BigDecimal totalFinal = BigDecimal.ZERO;

        private Integer quantidadeItens = 0;

        private List<String> formasPagamento = new ArrayList<>();
        private List<ItemDTO> itens = new ArrayList<>();
    }

    // Itens de cada venda
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {
        private Long id;
        private Long produtoId;
        private String nomeProduto;
        private BigDecimal quantidade = BigDecimal.ZERO;
        private BigDecimal precoUnitario = BigDecimal.ZERO;
        private BigDecimal subtotal = BigDecimal.ZERO;
    }
}

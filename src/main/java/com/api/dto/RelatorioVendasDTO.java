package com.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RelatorioVendasDTO {

    private ResumoDTO resumo;
    private List<GraficoItemDTO> grafico;
    private List<VendaDetalheDTO> vendas;

    public RelatorioVendasDTO(ResumoDTO resumo, List<GraficoItemDTO> grafico, List<VendaDetalheDTO> vendas) {
        this.resumo = resumo;
        this.grafico = grafico;
        this.vendas = vendas;
    }

    public ResumoDTO getResumo() { return resumo; }
    public List<GraficoItemDTO> getGrafico() { return grafico; }
    public List<VendaDetalheDTO> getVendas() { return vendas; }

    // --- Sub-DTOs estruturados ---
    public static class ResumoDTO {
        private BigDecimal totalFaturamento;
        private Long quantidadeVendas;
        private BigDecimal ticketMedio;

        public ResumoDTO(BigDecimal totalFaturamento, Long quantidadeVendas, BigDecimal ticketMedio) {
            this.totalFaturamento = totalFaturamento;
            this.quantidadeVendas = quantidadeVendas;
            this.ticketMedio = ticketMedio;
        }

        public BigDecimal getTotalFaturamento() { return totalFaturamento; }
        public Long getQuantidadeVendas() { return quantidadeVendas; }
        public BigDecimal getTicketMedio() { return ticketMedio; }
    }

    public static class GraficoItemDTO {
        private String data;
        private BigDecimal valor;

        public GraficoItemDTO(String data, BigDecimal valor) {
            this.data = data;
            this.valor = valor;
        }

        public String getData() { return data; }
        public BigDecimal getValor() { return valor; }
    }

    public static class VendaDetalheDTO {
        private Long id;
        private LocalDateTime dataVenda;
        private String clienteNome;
        private Integer quantidadeItens;
        private String metodoPagamento;
        private BigDecimal totalFinal;
        private List<ItemVendaDTO> itens;

        public VendaDetalheDTO(Long id, LocalDateTime dataVenda, String clienteNome, Integer quantidadeItens, String metodoPagamento, BigDecimal totalFinal, List<ItemVendaDTO> itens) {
            this.id = id;
            this.dataVenda = dataVenda;
            this.clienteNome = clienteNome;
            this.quantidadeItens = quantidadeItens;
            this.metodoPagamento = metodoPagamento;
            this.totalFinal = totalFinal;
            this.itens = itens;
        }

        public Long getId() { return id; }
        public LocalDateTime getDataVenda() { return dataVenda; }
        public String getClienteNome() { return clienteNome; }
        public Integer getQuantidadeItens() { return quantidadeItens; }
        public String getMetodoPagamento() { return metodoPagamento; }
        public BigDecimal getTotalFinal() { return totalFinal; }
        public List<ItemVendaDTO> getItens() { return itens; }
    }

    public static class ItemVendaDTO {
        private Long id;
        private String nomeProduto;
        private Integer quantidade;
        private BigDecimal subtotal;

        public ItemVendaDTO(Long id, String nomeProduto, Integer quantidade, BigDecimal subtotal) {
            this.id = id;
            this.nomeProduto = nomeProduto;
            this.quantidade = quantidade;
            this.subtotal = subtotal;
        }

        public Long getId() { return id; }
        public String getNomeProduto() { return nomeProduto; }
        public Integer getQuantidade() { return quantidade; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}
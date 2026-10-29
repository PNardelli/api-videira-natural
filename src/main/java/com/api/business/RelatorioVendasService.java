package com.api.business;

import com.api.dto.relatorio.PainelVendasDTO;
import com.api.eNum.StatusPedido;
import com.api.entity.ItemVenda;
import com.api.entity.PagamentoVenda;
import com.api.entity.Venda;
import com.api.repository.VendaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RelatorioVendasService {

    private final VendaRepository vendaRepository;

    public RelatorioVendasService(VendaRepository vendaRepository) {
        this.vendaRepository = vendaRepository;
    }

    @Transactional(readOnly = true)
    public PainelVendasDTO gerarRelatorio(String periodo, LocalDate inicio, LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        String filtro = periodo == null ? "HOJE" : periodo.trim().toUpperCase();

        LocalDate dataInicio;
        LocalDate dataFim;

        switch (filtro) {
            case "ONTEM" -> {
                dataInicio = hoje.minusDays(1);
                dataFim = dataInicio;
            }
            case "7_DIAS", "7DIAS", "SEMANA" -> {
                dataInicio = hoje.minusDays(6);
                dataFim = hoje;
            }
            case "30_DIAS", "30DIAS" -> {
                dataInicio = hoje.minusDays(29);
                dataFim = hoje;
            }
            case "PERSONALIZADO" -> {
                dataInicio = inicio != null ? inicio : hoje.minusDays(29);
                dataFim = fim != null ? fim : hoje;
            }
            case "MES" -> {
                dataInicio = hoje.withDayOfMonth(1);
                dataFim = hoje;
            }
            default -> {
                dataInicio = hoje;
                dataFim = hoje;
            }
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        LocalDateTime inicioPeriodo = dataInicio.atStartOfDay();
        LocalDateTime fimExclusivo = dataFim.plusDays(1).atStartOfDay();

        long totalVendas = vendaRepository.contarVendasPorPeriodo(inicioPeriodo, fimExclusivo);
        long finalizadas = vendaRepository.contarPorStatusEPeriodo(
                StatusPedido.FINALIZADO, inicioPeriodo, fimExclusivo);
        long canceladas = vendaRepository.contarPorStatusEPeriodo(
                StatusPedido.CANCELADO, inicioPeriodo, fimExclusivo);
        long pendentes = Math.max(0, totalVendas - finalizadas - canceladas);

        BigDecimal faturamento = valor(vendaRepository.somarPorStatusEPeriodo(
                StatusPedido.FINALIZADO, inicioPeriodo, fimExclusivo));
        BigDecimal ticketMedio = finalizadas > 0
                ? faturamento.divide(BigDecimal.valueOf(finalizadas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PainelVendasDTO dto = new PainelVendasDTO();
        dto.setPeriodo(new PainelVendasDTO.PeriodoDTO(
                dataInicio, dataFim,
                dataInicio.equals(dataFim) ? dataInicio.toString() : dataInicio + " até " + dataFim));

        PainelVendasDTO.ResumoDTO resumo = new PainelVendasDTO.ResumoDTO();
        resumo.setTotalFaturamento(faturamento);
        resumo.setQuantidadeVendas(totalVendas);
        resumo.setQuantidadeFinalizadas(finalizadas);
        resumo.setQuantidadeCanceladas(canceladas);
        resumo.setQuantidadePendentes(pendentes);
        resumo.setTicketMedio(ticketMedio);
        dto.setResumo(resumo);

        // Comparativo com período anterior de mesma duração.
        long diasPeriodo = ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
        LocalDateTime inicioAnterior = dataInicio.minusDays(diasPeriodo).atStartOfDay();
        LocalDateTime fimAnterior = dataInicio.atStartOfDay();

        BigDecimal faturamentoAnterior = valor(vendaRepository.somarPorStatusEPeriodo(
                StatusPedido.FINALIZADO, inicioAnterior, fimAnterior));
        long vendasAnteriores = vendaRepository.contarPorStatusEPeriodo(
                StatusPedido.FINALIZADO, inicioAnterior, fimAnterior);

        BigDecimal variacao = BigDecimal.ZERO;
        if (faturamentoAnterior.compareTo(BigDecimal.ZERO) != 0) {
            variacao = faturamento.subtract(faturamentoAnterior)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(faturamentoAnterior, 2, RoundingMode.HALF_UP);
        }

        PainelVendasDTO.ComparativoDTO comparativo = new PainelVendasDTO.ComparativoDTO();
        comparativo.setFaturamentoAnterior(faturamentoAnterior);
        comparativo.setVariacaoPercentual(variacao);
        comparativo.setVendasAnterior(vendasAnteriores);
        dto.setComparativo(comparativo);

        // Gráfico agregado no banco; completa dias sem vendas com zero.
        Map<LocalDate, PainelVendasDTO.GraficoDTO> graficoPorDia = new LinkedHashMap<>();
        for (Object[] linha : vendaRepository.agruparFaturamentoDiario(
                StatusPedido.FINALIZADO, inicioPeriodo, fimExclusivo)) {
            LocalDate dia = converterData(linha[0]);
            BigDecimal valorDia = valor((BigDecimal) linha[1]);
            long quantidadeDia = ((Number) linha[2]).longValue();
            graficoPorDia.put(dia, new PainelVendasDTO.GraficoDTO(dia, valorDia, quantidadeDia));
        }

        List<PainelVendasDTO.GraficoDTO> grafico = new ArrayList<>();
        for (LocalDate dia = dataInicio; !dia.isAfter(dataFim); dia = dia.plusDays(1)) {
            grafico.add(graficoPorDia.getOrDefault(
                    dia, new PainelVendasDTO.GraficoDTO(dia, BigDecimal.ZERO, 0L)));
        }
        dto.setGrafico(grafico);

        // Formas de pagamento agregadas no banco.
        List<PainelVendasDTO.FormaPagamentoDTO> formas = new ArrayList<>();
        for (Object[] linha : vendaRepository.agruparPagamentos(
                StatusPedido.FINALIZADO, inicioPeriodo, fimExclusivo)) {
            String forma = linha[0] != null ? linha[0].toString() : "NAO_INFORMADO";
            BigDecimal valorForma = valor((BigDecimal) linha[1]);
            long quantidade = ((Number) linha[2]).longValue();
            BigDecimal percentual = faturamento.signum() > 0
                    ? valorForma.multiply(BigDecimal.valueOf(100))
                    .divide(faturamento, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            formas.add(new PainelVendasDTO.FormaPagamentoDTO(
                    forma, valorForma, percentual, quantidade));
        }
        dto.setFormasPagamento(formas);

        // Produtos mais vendidos agregados no banco.
        List<PainelVendasDTO.ProdutoMaisVendidoDTO> produtos = new ArrayList<>();
        for (Object[] linha : vendaRepository.agruparProdutosVendidos(
                StatusPedido.FINALIZADO, inicioPeriodo, fimExclusivo)) {
            produtos.add(new PainelVendasDTO.ProdutoMaisVendidoDTO(
                    ((Number) linha[0]).longValue(),
                    linha[1] != null ? linha[1].toString() : "Produto",
                    valor((BigDecimal) linha[2]),
                    valor((BigDecimal) linha[3])));
        }
        produtos.sort(Comparator.comparing(
                PainelVendasDTO.ProdutoMaisVendidoDTO::getQuantidadeVendida).reversed());
        dto.setProdutosMaisVendidos(produtos.stream().limit(10).toList());

        // Somente as 10 vendas mais recentes para a tabela do painel.
        List<Venda> recentes = vendaRepository.buscarVendasRecentes(
                inicioPeriodo, fimExclusivo, PageRequest.of(0, 10));
        dto.setVendas(recentes.stream().map(this::mapearVenda).toList());

        return dto;
    }

    private PainelVendasDTO.VendaDTO mapearVenda(Venda venda) {
        PainelVendasDTO.VendaDTO dto = new PainelVendasDTO.VendaDTO();
        dto.setId(venda.getId());
        dto.setDataVenda(venda.getDataVenda());
        dto.setClienteNome(venda.getCliente() != null ? venda.getCliente().getNome() : "Cliente Balcão");
        dto.setStatus(venda.getStatusPedido() != null ? venda.getStatusPedido().name() : "INDEFINIDO");
        dto.setCanalVenda(venda.getCanalVenda() != null ? venda.getCanalVenda().name() : "INDEFINIDO");
        dto.setSubtotal(valor(venda.getSubtotal()));
        dto.setDesconto(valor(venda.getDesconto()));
        dto.setFrete(valor(venda.getFrete()));
        dto.setTotalFinal(valor(venda.getTotal()));

        List<ItemVenda> itens = venda.getItens() != null ? venda.getItens() : List.of();
//        dto.setQuantidadeItens(itens.stream()
//                .map(ItemVenda::getQuantidade)
//                .filter(Objects::nonNull)
//                .reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setQuantidadeItens(itens.size());

        dto.setFormasPagamento(venda.getPagamentos() == null ? List.of()
                : venda.getPagamentos().stream()
                .filter(Objects::nonNull)
                .map(PagamentoVenda::getTipo)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .distinct()
                .toList());

        dto.setItens(itens.stream()
                .map(item -> new PainelVendasDTO.ItemDTO(
                        item.getId(),
                        item.getProduto() != null ? item.getProduto().getId() : null,
                        item.getProduto() != null ? item.getProduto().getNome() : "Produto",
                        valor(item.getQuantidade()),
                        valor(item.getPrecoUnitario()),
                        valor(item.getSubtotal())))
                .toList());

        return dto;
    }

    private LocalDate converterData(Object valorData) {
        if (valorData instanceof LocalDate data) return data;
        if (valorData instanceof Date dataSql) return dataSql.toLocalDate();
        if (valorData instanceof java.util.Date data) {
            return new Date(data.getTime()).toLocalDate();
        }
        return LocalDate.parse(valorData.toString());
    }

    private BigDecimal valor(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
package com.api.business;

import com.api.dto.financeiro.FaturamentoAnoDTO;
import com.api.dto.financeiro.FaturamentoManualRequestDTO;
import com.api.dto.financeiro.MensalidadeRequestDTO;
import com.api.dto.relatorio.PainelVendasDTO;
import com.api.entity.FaturamentoMensal;
import com.api.entity.MensalidadeSistema;
import com.api.repository.FaturamentoMensalRepository;
import com.api.repository.MensalidadeSistemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceiroService {

    private final FaturamentoMensalRepository faturamentoRepository;
    private final MensalidadeSistemaRepository mensalidadeRepository;
    private final RelatorioVendasService relatorioVendasService;

    public FinanceiroService(
            FaturamentoMensalRepository faturamentoRepository,
            MensalidadeSistemaRepository mensalidadeRepository,
            RelatorioVendasService relatorioVendasService) {
        this.faturamentoRepository = faturamentoRepository;
        this.mensalidadeRepository = mensalidadeRepository;
        this.relatorioVendasService = relatorioVendasService;
    }

    @Transactional(readOnly = true)
    public FaturamentoAnoDTO obterFaturamentoAno(int ano) {
        Map<Integer, FaturamentoMensal> manuaisPorMes = faturamentoRepository
                .findByAnoOrderByMesAsc(ano)
                .stream()
                .collect(Collectors.toMap(FaturamentoMensal::getMes, f -> f, (a, b) -> b));

        List<FaturamentoAnoDTO.FaturamentoMesDTO> mesesDto = new ArrayList<>();
        BigDecimal totalAnoAtual = BigDecimal.ZERO;

        for (int mes = 1; mes <= 12; mes++) {
            FaturamentoAnoDTO.FaturamentoMesDTO dto = new FaturamentoAnoDTO.FaturamentoMesDTO();
            dto.setMes(mes);

            FaturamentoMensal manual = manuaisPorMes.get(mes);
            if (manual != null) {
                BigDecimal valor = nvl(manual.getValor());
                long pedidos = manual.getQuantidadePedidos() != null ? manual.getQuantidadePedidos() : 0L;
                dto.setFaturamento(valor);
                dto.setPedidos(pedidos);
                dto.setTicketMedio(pedidos > 0
                        ? valor.divide(BigDecimal.valueOf(pedidos), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
                dto.setOrigem("MANUAL");
                dto.setDataReferencia(manual.getDataReferencia() != null
                        ? manual.getDataReferencia()
                        : LocalDate.of(ano, mes, 1));
            } else {
                YearMonth ym = YearMonth.of(ano, mes);
                LocalDate inicio = ym.atDay(1);
                LocalDate fim = ym.atEndOfMonth();

                PainelVendasDTO painel = relatorioVendasService.gerarRelatorio("PERSONALIZADO", inicio, fim);
                PainelVendasDTO.ResumoDTO resumo = painel != null ? painel.getResumo() : null;

                BigDecimal valor = resumo != null ? nvl(resumo.getTotalFaturamento()) : BigDecimal.ZERO;
                long pedidos = resumo != null && resumo.getQuantidadeFinalizadas() != null
                        ? resumo.getQuantidadeFinalizadas()
                        : 0L;

                dto.setFaturamento(valor);
                dto.setPedidos(pedidos);
                dto.setTicketMedio(pedidos > 0
                        ? valor.divide(BigDecimal.valueOf(pedidos), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
                dto.setOrigem("VENDAS");
                dto.setDataReferencia(inicio);
            }

            totalAnoAtual = totalAnoAtual.add(dto.getFaturamento());
            mesesDto.add(dto);
        }

        BigDecimal totalAnoAnterior = calcularTotalAno(ano - 1);
        BigDecimal variacaoAno = null;
        if (totalAnoAnterior.compareTo(BigDecimal.ZERO) > 0) {
            variacaoAno = totalAnoAtual.subtract(totalAnoAnterior)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalAnoAnterior, 1, RoundingMode.HALF_UP);
        }

        FaturamentoAnoDTO resposta = new FaturamentoAnoDTO();
        resposta.setAno(ano);
        resposta.setVariacaoAnoPercentual(variacaoAno);
        resposta.setMeses(mesesDto);
        return resposta;
    }

    private BigDecimal calcularTotalAno(int ano) {
        Map<Integer, FaturamentoMensal> manuais = faturamentoRepository
                .findByAnoOrderByMesAsc(ano)
                .stream()
                .collect(Collectors.toMap(FaturamentoMensal::getMes, f -> f, (a, b) -> b));

        BigDecimal total = BigDecimal.ZERO;
        for (int mes = 1; mes <= 12; mes++) {
            FaturamentoMensal manual = manuais.get(mes);
            if (manual != null) {
                total = total.add(nvl(manual.getValor()));
            } else {
                YearMonth ym = YearMonth.of(ano, mes);
                PainelVendasDTO painel = relatorioVendasService.gerarRelatorio(
                        "PERSONALIZADO", ym.atDay(1), ym.atEndOfMonth());
                if (painel != null && painel.getResumo() != null) {
                    total = total.add(nvl(painel.getResumo().getTotalFaturamento()));
                }
            }
        }
        return total;
    }

    @Transactional
    public FaturamentoMensal salvarFaturamentoManual(FaturamentoManualRequestDTO req) {
        if (req.getAno() == null || req.getMes() == null || req.getMes() < 1 || req.getMes() > 12) {
            throw new IllegalArgumentException("Ano e mês (1 a 12) são obrigatórios.");
        }

        FaturamentoMensal entidade = faturamentoRepository
                .findByAnoAndMes(req.getAno(), req.getMes())
                .orElseGet(FaturamentoMensal::new);

        entidade.setAno(req.getAno());
        entidade.setMes(req.getMes());
        entidade.setValor(nvl(req.getValor()));
        entidade.setQuantidadePedidos(req.getQuantidadePedidos() != null ? req.getQuantidadePedidos() : 0L);
        entidade.setDataReferencia(req.getDataReferencia() != null
                ? req.getDataReferencia()
                : LocalDate.of(req.getAno(), req.getMes(), 1));

        return faturamentoRepository.save(entidade);
    }

    @Transactional(readOnly = true)
    public List<MensalidadeSistema> listarMensalidadesAno(int ano) {
        return mensalidadeRepository.findByAnoOrderByMesAsc(ano);
    }

    @Transactional
    public MensalidadeSistema salvarMensalidade(int ano, int mes, MensalidadeRequestDTO req) {
        MensalidadeSistema entidade = mensalidadeRepository
                .findByAnoAndMes(ano, mes)
                .orElseGet(MensalidadeSistema::new);

        boolean pago = Boolean.TRUE.equals(req.getPago());
        entidade.setAno(ano);
        entidade.setMes(mes);
        entidade.setValor(nvl(req.getValor()));
        entidade.setPago(pago);
        entidade.setDataVencimento(req.getDataVencimento() != null
                ? req.getDataVencimento()
                : LocalDate.of(ano, mes, 5));
        entidade.setDataPagamento(pago ? req.getDataPagamento() : null);
        entidade.setDataReferencia(req.getDataReferencia() != null
                ? req.getDataReferencia()
                : LocalDate.of(ano, mes, 1));

        return mensalidadeRepository.save(entidade);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

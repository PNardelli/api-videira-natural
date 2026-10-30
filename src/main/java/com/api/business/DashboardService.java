package com.api.business;

import com.api.dto.DashboardDTO;
import com.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class DashboardService {

    @Autowired
    private ProdutoFornecedorRepository pfRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VendaRepository vendaRepository;

    public DashboardDTO buscarDadosDashboard() {
        DashboardDTO dto = new DashboardDTO();

        // 1. Contagens Básicas
        dto.setTotalClientes(clienteRepository.count());
        dto.setTotalProdutos(produtoRepository.count());

        // Referência do dia atual (00:00:00) para a Home
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        LocalDateTime agora = hoje.atStartOfDay();

        // Diário: hoje
        LocalDateTime inicioDia = hoje.atStartOfDay();

        BigDecimal diario = vendaRepository.somarVendasFinalizadasPorPeriodo(
                inicioDia,
                inicioDia.plusDays(1)
        );
        dto.setFaturamentoDiario(diario);

        // Semanal: segunda-feira até a próxima segunda-feira
        LocalDate segundaFeira = hoje.with(DayOfWeek.MONDAY);
        LocalDateTime inicioSemana = segundaFeira.atStartOfDay();

        BigDecimal semanal = vendaRepository.somarVendasFinalizadasPorPeriodo(
                inicioSemana,
                inicioSemana.plusWeeks(1)
        );
        dto.setFaturamentoSemanal(semanal);

        // Mensal: primeiro dia até o primeiro dia do próximo mês
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();

        BigDecimal mensal = vendaRepository.somarVendasFinalizadasPorPeriodo(
                inicioMes,
                inicioMes.plusMonths(1)
        );
        dto.setFaturamentoMensal(mensal);
        // ---------------------------------------------------------------------------

        // 3. Alertas de Validade (Regra: Vence em até 15 dias)
        dto.setProdutosVencendo(pfRepository.buscarProdutosVencendo(LocalDate.from(agora.plusDays(15))));

        // 4. Alertas de Estoque Baixo (Recuperar pelo minino cadastrado.)
        dto.setEstoqueBaixo(pfRepository.buscarEstoqueBaixo());

        // 5. Alertas de Sem Estoque.
        dto.setSemEstoque(pfRepository.buscarSemEstoque());

        return dto;
    }
}
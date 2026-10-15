package com.api.business;

import com.api.dto.DashboardDTO;
import com.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;

@Service
public class DashboardService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private ProdutoFornecedorRepository pfRepository;
    @Autowired
    private VendaRepository vendaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;

    public DashboardDTO buscarDadosDashboard() {
        DashboardDTO dto = new DashboardDTO();

        // Referência do dia atual (00:00:00) para a Home
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioDia = agora.truncatedTo(ChronoUnit.DAYS);

        // 1. Contagens Básicas
        dto.setTotalClientes(clienteRepository.count());
        dto.setTotalProdutos(produtoRepository.count());
        dto.setTotalFornecedores(fornecedorRepository.count());

        // 2. Faturamento Diário (Exibido atualmente na Home)
        BigDecimal diario = vendaRepository.somarVendasAPartirDe(inicioDia);
        dto.setFaturamentoDiario(diario != null ? diario : BigDecimal.ZERO);

        // --- CAMINHO PREPARADO PARA O FUTURO DASHBOARD (Já populando o DTO no back) ---
        LocalDateTime inicioSemana = agora.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime inicioMes = agora.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

        BigDecimal semanal = vendaRepository.somarVendasAPartirDe(inicioSemana);
        BigDecimal mensal = vendaRepository.somarVendasAPartirDe(inicioMes);

        dto.setFaturamentoSemanal(semanal != null ? semanal : BigDecimal.ZERO);
        dto.setFaturamentoMensal(mensal != null ? mensal : BigDecimal.ZERO);
        // ---------------------------------------------------------------------------

        // 3. Alertas de Validade (Regra: Vence em até 15 dias)
        dto.setProdutosVencendo(pfRepository.buscarProdutosVencendo(LocalDate.from(agora.plusDays(15))));

        // 4. Alertas de Estoque Baixo (Regra: Menos de 1kg ou 1 unidade)
        dto.setEstoqueBaixo(pfRepository.buscarEstoqueBaixo(new BigDecimal("1.0")));

        return dto;
    }
}
package com.api.business;

import com.api.dto.DashboardDTO;
import com.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        LocalDateTime hoje = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

        // 1. Contagens Básicas
        dto.setTotalClientes(clienteRepository.count());
        dto.setTotalProdutos(produtoRepository.count());
        dto.setTotalFornecedores(fornecedorRepository.count());

        // 2. Faturamento Periódico
        dto.setFaturamentoDiario(vendaRepository.calcularFaturamento(hoje, LocalDateTime.now()));
        dto.setFaturamentoSemanal(vendaRepository.calcularFaturamento(hoje.minusDays(7), LocalDateTime.now()));
        dto.setFaturamentoMensal(vendaRepository.calcularFaturamento(hoje.withDayOfMonth(1), LocalDateTime.now()));

        // 3. Alertas de Validade (Regra: Vence em até 15 dias)
        dto.setProdutosVencendo(pfRepository.buscarProdutosVencendo(LocalDate.from(LocalDateTime.now().plusDays(15))));

        // 4. Alertas de Estoque Baixo (Regra: Menos de 1kg ou 1 unidade)
        dto.setEstoqueBaixo(pfRepository.buscarEstoqueBaixo(new BigDecimal("1.0")));

        return dto;
    }
}

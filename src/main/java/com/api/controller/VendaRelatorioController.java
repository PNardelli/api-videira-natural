package com.api.controller;

import com.api.business.VendaService;
import com.api.dto.RelatorioVendasDTO;
import com.api.dto.historicoVendas.VendaHistoricoDTO;
import com.api.entity.Venda;
import com.api.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendas")
public class VendaRelatorioController {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private VendaService vendaService;

    @GetMapping("/relatorio-completo")
    public ResponseEntity<RelatorioVendasDTO> gerarRelatorio(
            @RequestParam(value = "periodo", defaultValue = "HOJE") String periodo,
            @RequestParam(value = "inicio", required = false) String inicio,
            @RequestParam(value = "fim", required = false) String fim) {

        LocalDateTime dataInicio;
        LocalDateTime dataFim = LocalDateTime.now();

        if (periodo == null) periodo = "HOJE";

        switch (periodo.toUpperCase()) {
            case "SEMANA":
                dataInicio = LocalDateTime.now().minusDays(7);
                break;
            case "MES":
                dataInicio = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
            case "PERSONALIZADO":
                dataInicio = (inicio != null && !inicio.trim().isEmpty()) ? LocalDate.parse(inicio).atStartOfDay() : LocalDateTime.now().minusDays(30);
                dataFim = (fim != null && !fim.trim().isEmpty()) ? LocalDate.parse(fim).atTime(LocalTime.MAX) : LocalDateTime.now();
                break;
            case "HOJE":
            default:
                dataInicio = LocalDate.now().atStartOfDay();
                break;
        }

        List<Venda> vendas = vendaRepository.findByDataVendaBetween(dataInicio, dataFim);

        // Cálculos do Resumo
        BigDecimal totalFaturamento = vendas.stream()
                .map(Venda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long qtdVendas = vendas.size();
        BigDecimal ticketMedio = qtdVendas > 0
                ? totalFaturamento.divide(BigDecimal.valueOf(qtdVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        RelatorioVendasDTO.ResumoDTO resumoDto = new RelatorioVendasDTO.ResumoDTO(totalFaturamento, qtdVendas, ticketMedio);

        // Mapeamento Detalhado das Vendas
        List<RelatorioVendasDTO.VendaDetalheDTO> listaDetalhes = vendas.stream().map(v -> {
            String clienteNome = v.getCliente() != null ? v.getCliente().getNome() : "Cliente Balcão";
            String formaPagamento = (v.getPagamentos() != null && !v.getPagamentos().isEmpty() && v.getPagamentos().get(0).getTipo() != null)
                    ? v.getPagamentos().get(0).getTipo().name() : "DINHEIRO";

            List<RelatorioVendasDTO.ItemVendaDTO> itensDto = v.getItens() != null ? v.getItens().stream().map(i -> new RelatorioVendasDTO.ItemVendaDTO(
                    i.getId(),
                    i.getProduto() != null ? i.getProduto().getNome() : "Produto",
                    i.getQuantidade() != null ? i.getQuantidade().intValue() : 1,
                    i.getSubtotal()
            )).collect(Collectors.toList()) : new ArrayList<>();

            return new RelatorioVendasDTO.VendaDetalheDTO(
                    v.getId(),
                    v.getDataVenda(),
                    clienteNome,
                    v.getItens() != null ? v.getItens().size() : 0,
                    formaPagamento,
                    v.getTotal(),
                    itensDto
            );
        }).collect(Collectors.toList());

        // Mapeamento do Gráfico Diário
        Map<LocalDate, BigDecimal> agrupadoPorDia = vendas.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getDataVenda().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Venda::getTotal, BigDecimal::add)
                ));

        List<RelatorioVendasDTO.GraficoItemDTO> graficoDto = new ArrayList<>();
        agrupadoPorDia.forEach((data, valor) -> {
            graficoDto.add(new RelatorioVendasDTO.GraficoItemDTO(data.toString(), valor));
        });

        RelatorioVendasDTO respostaFinal = new RelatorioVendasDTO(resumoDto, graficoDto, listaDetalhes);
        return ResponseEntity.ok(respostaFinal);
    }

    /**
     * VENDA PARA DASHBOARD
     */
    @Transactional(readOnly = true)
    @GetMapping("/historico-geral")
    public ResponseEntity<List<VendaHistoricoDTO>> listarHistoricoVendas(@RequestParam(required = false, name = "termo") String termo) {
        List<Venda> vendas = vendaService.listarHistoricoGeral(termo);

        // Converte a lista de entidades Venda para o DTO limpo
        List<VendaHistoricoDTO> dtos = vendas.stream()
                .map(VendaHistoricoDTO::de)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Transactional
    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarVenda(@PathVariable Long id) {
        try {
            vendaService.cancelarVenda(id);
            return ResponseEntity.ok().body("Venda cancelada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cancelar a venda: " + e.getMessage());
        }
    }

}
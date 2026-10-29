package com.api.controller;

import com.api.business.RelatorioVendasService;
import com.api.business.VendaService;
import com.api.dto.relatorio.PainelVendasDTO;
import com.api.dto.historicoVendas.VendaHistoricoDTO;
import com.api.entity.Venda;
import com.api.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendas")
public class VendaRelatorioController {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private RelatorioVendasService relatorioVendasService;

    /**
     * RELATÓRIO COMPLETO DE VENDAS
     */
    @GetMapping("/relatorio-completo")
    public ResponseEntity<PainelVendasDTO> gerarRelatorio(
            @RequestParam(value = "periodo", defaultValue = "HOJE") String periodo,
            @RequestParam(value = "inicio", required = false) String inicio,
            @RequestParam(value = "fim", required = false) String fim) {

        LocalDate dataInicio = null;
        LocalDate dataFim = null;

        if (inicio != null && !inicio.trim().isEmpty()) {
            dataInicio = LocalDate.parse(inicio);
        }

        if (fim != null && !fim.trim().isEmpty()) {
            dataFim = LocalDate.parse(fim);
        }

        PainelVendasDTO relatorio =
                relatorioVendasService.gerarRelatorio(
                        periodo,
                        dataInicio,
                        dataFim
                );

        return ResponseEntity.ok(relatorio);
    }

    /**
     * HISTÓRICO GERAL DE VENDAS
     */
    @Transactional(readOnly = true)
    @GetMapping("/historico-geral")
    public ResponseEntity<List<VendaHistoricoDTO>> listarHistoricoVendas(
            @RequestParam(required = false, name = "termo") String termo) {

        List<Venda> vendas = vendaService.listarVendasDoisDias(termo);

        List<VendaHistoricoDTO> dtos = vendas.stream()
                .map(VendaHistoricoDTO::de)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * PESQUISAR VENDAS POR ID OU CLIENTE
     */
    @GetMapping("/pesquisar-cliente-id")
    public List<Venda> pesquisarVendasPorIdOuCliente(
            @RequestParam(required = false) String termo) {

        return vendaService.pesquisarVendasPorIdOuCliente(termo);
    }

    /**
     * CANCELAR VENDA
     */
    @Transactional
    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarVenda(@PathVariable Long id) {
        try {
            vendaService.cancelarVenda(id);
            return ResponseEntity.ok()
                    .body("Venda cancelada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cancelar a venda: " + e.getMessage());
        }
    }
}
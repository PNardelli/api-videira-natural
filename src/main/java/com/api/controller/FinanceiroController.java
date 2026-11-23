package com.api.controller;

import com.api.business.FinanceiroService;
import com.api.dto.financeiro.FaturamentoAnoDTO;
import com.api.dto.financeiro.FaturamentoManualRequestDTO;
import com.api.dto.financeiro.MensalidadeRequestDTO;
import com.api.entity.FaturamentoMensal;
import com.api.entity.MensalidadeSistema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/faturamento")
    public ResponseEntity<FaturamentoAnoDTO> getFaturamentoAno(
            @RequestParam(required = false) Integer ano) {
        int anoAlvo = ano != null ? ano : LocalDate.now().getYear();
        return ResponseEntity.ok(financeiroService.obterFaturamentoAno(anoAlvo));
    }

    @PostMapping("/faturamento")
    public ResponseEntity<FaturamentoMensal> salvarFaturamentoManual(
            @RequestBody FaturamentoManualRequestDTO req) {
        return ResponseEntity.ok(financeiroService.salvarFaturamentoManual(req));
    }

    @GetMapping("/mensalidades")
    public ResponseEntity<List<MensalidadeSistema>> listarMensalidades(
            @RequestParam(required = false) Integer ano) {
        int anoAlvo = ano != null ? ano : LocalDate.now().getYear();
        return ResponseEntity.ok(financeiroService.listarMensalidadesAno(anoAlvo));
    }

    @PutMapping("/mensalidades/{ano}/{mes}")
    public ResponseEntity<MensalidadeSistema> salvarMensalidade(
            @PathVariable int ano,
            @PathVariable int mes,
            @RequestBody MensalidadeRequestDTO req) {
        return ResponseEntity.ok(financeiroService.salvarMensalidade(ano, mes, req));
    }
}
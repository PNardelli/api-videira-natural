package com.api.controller;

import com.api.entity.Venda;
import com.api.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/vendas")
public class HistoricoVendaController {

    @Autowired
    private VendaRepository vendaRepository;

    // Endpoint para buscar o histórico filtrado por data na nova view
    @GetMapping("/historico")
    public ResponseEntity<List<Venda>> listarHistorico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        LocalDate inicio = dataInicio != null ? dataInicio : LocalDate.now();
        LocalDate fim = dataFim != null ? dataFim : LocalDate.now();

        LocalDateTime inicioHora = inicio.atStartOfDay();
        LocalDateTime fimHora = fim.atTime(23, 59, 59);

        // Certifique-se de ter esse método no seu VendaRepository
        List<Venda> vendas = vendaRepository.findByDataVendaBetweenOrderByDataVendaDesc(inicioHora, fimHora);
        return ResponseEntity.ok(vendas);
    }
}

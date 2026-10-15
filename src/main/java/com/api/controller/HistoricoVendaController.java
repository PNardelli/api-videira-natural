package com.api.controller;

import com.api.dto.historicoVendas.VendaHistoricoDTO;
import com.api.entity.Venda;
import com.api.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/vendas")
public class HistoricoVendaController {

    @Autowired
    private VendaRepository vendaRepository;

    @Transactional(readOnly = true)
    @GetMapping("/historico")
    public ResponseEntity<List<VendaHistoricoDTO>> listarHistorico(@RequestParam(required = false) String termo) {

        // 1. Define o período do dia atual para a busca padrão
        LocalDateTime inicioHora = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimHora = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Venda> vendas;
        if (termo != null && !termo.trim().isEmpty()) {
            vendas = vendaRepository.buscarVendasGerais(termo);
        } else {
            vendas = vendaRepository.findByDataVendaBetweenOrderByDataVendaDesc(inicioHora, fimHora);
        }

        // Mapeamento limpo direto para o Record VendaHistoricoDTO
        List<VendaHistoricoDTO> dtos = vendas.stream()
                .map(VendaHistoricoDTO::de)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
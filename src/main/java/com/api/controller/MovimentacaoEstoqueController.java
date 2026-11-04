package com.api.controller;

import com.api.entity.MovimentacaoEstoque;
import com.api.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoEstoqueController {

    @Autowired
    private MovimentacaoRepository repository;

    @GetMapping("/listar")
    public ResponseEntity<List<MovimentacaoEstoque>> listarMovimentacoes(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes
    ) {
        List<MovimentacaoEstoque> lista;

        // Se o usuário passou o ano e o mês, filtra pelo período exato
        if (ano != null && mes != null) {
            LocalDateTime inicio = LocalDate.of(ano, mes, 1).atStartOfDay();
            LocalDateTime fim = LocalDate.of(ano, mes, 1).plusMonths(1).minusDays(1).atTime(LocalTime.MAX);
            lista = repository.buscarPorPeriodo(inicio, fim);
        } else {
            // Caso contrário, traz todas
            lista = repository.findAllByOrderByDataDesc();
        }

        return ResponseEntity.ok(lista);
    }
}
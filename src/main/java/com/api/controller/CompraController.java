package com.api.controller;

import com.api.business.CompraService;
import com.api.dto.CompraDTO;
import com.api.dto.CompraDetalheDTO;
import com.api.dto.CompraResumoDTO;
import com.api.entity.Compra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping("/salvar")
    public ResponseEntity<Compra> salvarCompra(@RequestBody CompraDTO dto) {
        Compra novaCompra = compraService.salvarCompra(dto);
        return ResponseEntity.ok(novaCompra);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<CompraResumoDTO>> listar() {
        // Certifique-se de que chama o serviço que usa a query do DTO
        List<CompraResumoDTO> lista = compraService.listarResumo();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDetalheDTO> buscarPorId(@PathVariable Long id) {
        CompraDetalheDTO detalhe = compraService.buscarDetalhesPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @PostMapping("/{id}/previsao/{previsao}")
    public ResponseEntity<Compra> ajustarPrevisao(@PathVariable Long id, LocalDate previsao) {
        Compra response = compraService.ajustarPrevisao(id, previsao);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/receber")
    public ResponseEntity<Void> confirmarRecebimento(@PathVariable Long id) {
        compraService.processarRecebimentoEstoque(id);
        return ResponseEntity.ok().build();
    }
}
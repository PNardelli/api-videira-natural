package com.api.controller;

import com.api.business.CompraService;
import com.api.dto.CompraDTO;
import com.api.dto.CompraResumoDTO;
import com.api.entity.Compra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
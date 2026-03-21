package com.api.controller;

import com.api.business.EstoqueService;
import com.api.dto.MovimentacaoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    @Autowired private EstoqueService estoqueService;

    @PostMapping("/movimentar")
    public ResponseEntity<?> movimentar(@RequestBody MovimentacaoDTO dto) {
        try {
            estoqueService.processarMovimentacao(dto);
            return ResponseEntity.ok("ESTOQUE ATUALIZADO!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

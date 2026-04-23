package com.api.controller;

import com.api.business.EstoqueService;
import com.api.business.ProdutoFornecedorService;
import com.api.dto.MovimentacaoDTO;
import com.api.entity.ProdutoFornecedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    @Autowired private EstoqueService estoqueService;

    @Autowired
    private ProdutoFornecedorService pfService;

    @PostMapping("/movimentar")
    public ResponseEntity<?> movimentar(@RequestBody MovimentacaoDTO dto) {
        try {
            estoqueService.processarMovimentacao(dto);
            return ResponseEntity.ok("ESTOQUE ATUALIZADO!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/produto-fornecedor/{id}")
    public ProdutoFornecedor buscar(@PathVariable Long id){
        return pfService.recuperarProduto(id);
    }


    @PostMapping("/nova-entrada")
    public ResponseEntity<?> novaEntrada(@RequestBody MovimentacaoDTO dto) {
        try {
            estoqueService.processarNovaEntrada(dto);
            return ResponseEntity.ok("NOVA ENTRADA!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

package com.api.controller;

import com.api.business.PromocaoService;
import com.api.entity.Promocao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/promocoes")
public class PromocaoController {

    @Autowired
    private PromocaoService promocaoService;

    @PostMapping("/criar")
        public Promocao criarPromocao(@RequestBody Promocao dto){
            return promocaoService.criarPromocao(dto);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Promocao>> listarPromocoes() {
        List<Promocao> lista = promocaoService.listarPromocoes();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPromocao(
            @PathVariable Long id,
            @RequestParam("quantidade") long quantidade) {
        promocaoService.cancelarPromocao(id, quantidade);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/excluir")
    public ResponseEntity<Void> excluirPromocao(
            @PathVariable Long id) {
        promocaoService.excluirPromocao(id);
        return ResponseEntity.ok().build();
    }
}

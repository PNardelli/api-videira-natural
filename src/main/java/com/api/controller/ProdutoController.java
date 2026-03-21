package com.api.controller;

import com.api.dto.MovimentacaoDTO;
import com.api.dto.ProdutoRequest;
import com.api.entity.Produto;
import com.api.business.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin("*")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping("/cadastrar")
    public Produto salvar(@RequestBody ProdutoRequest produtoRequest){
        return produtoService.salvar(produtoRequest);
    }

    @GetMapping("/listar")
    public List<Produto> listar(){
        return produtoService.listar();
    }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable Long id){
        return produtoService.buscar(id);
    }

    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable Long id, @RequestBody ProdutoRequest produtoRequest){
        produtoService.atualizarProduto(id, produtoRequest);
        return produtoService.buscar(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id){
        produtoService.deletar(id);
    }

}
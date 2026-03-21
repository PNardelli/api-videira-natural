package com.api.controller;

import com.api.entity.Fornecedor;
import com.api.business.FornecedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
@CrossOrigin
public class FornecedorController {

    private final FornecedorService service;

    public FornecedorController(FornecedorService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> salvar(@RequestBody Fornecedor fornecedor) {
        try {
            return service.salvar(fornecedor);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/listar")
    public List<Fornecedor> listar() {
        return service.listar();
    }

}
package com.api.controller;

import com.api.business.CategoriaService;
import com.api.entity.Categoria;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    public Categoria salvar(@RequestBody Categoria categoria) {
        return service.salvar(categoria);
    }

    @GetMapping
    public List<Categoria> listar() {
        return service.listar();
    }

}

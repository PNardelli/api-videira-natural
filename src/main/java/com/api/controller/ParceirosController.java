package com.api.controller;

import com.api.business.ParceiroService;
import com.api.dto.ParceirosDTO;
import com.api.entity.Parceiros;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parceiros")
@CrossOrigin(origins = "*") // libera para o Vue local
public class ParceirosController {

    @Autowired
    ParceiroService parceiroService;

    // Criar Parceiro
    @PostMapping
    public ResponseEntity<Parceiros> criar(@RequestBody ParceirosDTO parceirosDTO) {
        Parceiros salvo = parceiroService.salvarParceiro(parceirosDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // Listar todos
    @GetMapping("/buscar")
    public List<Parceiros> buscar() {
            return parceiroService.listarTodos();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Parceiros> atualizar(@PathVariable Long id, @RequestBody Parceiros parceirosDTO){
        Parceiros atualizado = parceiroService.atualizarParceiros(id, parceirosDTO);
        return ResponseEntity.ok(atualizado);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id) {
        parceiroService.alterarStatus(id);
        return ResponseEntity.noContent().build();
    }

}


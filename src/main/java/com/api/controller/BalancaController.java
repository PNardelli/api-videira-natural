package com.api.controller;

import com.api.business.BalancaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/balanca")
public class BalancaController {

    @Autowired
    private BalancaService balancaService;

    @GetMapping("/peso")
    public ResponseEntity<?> getPeso() {
        return ResponseEntity.ok(Map.of(
                "peso", balancaService.getPeso(),
                "estavel", balancaService.isEstavel()
        ));
    }

    @PostMapping("/iniciar")
    public void iniciar() {
        balancaService.iniciarLeitura();
    }

    @PostMapping("/parar")
    public void parar() {
        balancaService.pararLeitura();
    }
}

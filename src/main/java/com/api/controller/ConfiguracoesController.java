package com.api.controller;

import com.api.business.ConfiguracaoFreteService;
import com.api.dto.ConfiguracaoFreteDTO;
import com.api.entity.ConfiguracaoFrete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/configuracoes")
@CrossOrigin("*")
public class ConfiguracoesController {

    @Autowired
    private ConfiguracaoFreteService service;

    @GetMapping("/frete")
    public ResponseEntity<ConfiguracaoFrete> buscar() {
        ConfiguracaoFrete config = service.obterConfiguracao();
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @PutMapping("/cadastrar-frete")
    public ResponseEntity<ConfiguracaoFrete> salvar(@RequestBody ConfiguracaoFreteDTO dto) {
        ConfiguracaoFrete salva = service.salvarOuAtualizar(dto);
        return ResponseEntity.ok(salva);
    }
}

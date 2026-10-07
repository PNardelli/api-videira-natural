package com.api.controller;

import com.api.business.DashboardService;
import com.api.dto.DashboardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard") // Caminho base
@CrossOrigin(origins = "*")      // Para evitar erros de CORS no navegador
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/resumo") // Complemento do caminho
    public ResponseEntity<DashboardDTO> getResumo() {
        return ResponseEntity.ok(dashboardService.buscarDadosDashboard());
    }
}
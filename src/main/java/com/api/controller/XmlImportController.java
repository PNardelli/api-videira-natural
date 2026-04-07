package com.api.controller;

import com.api.business.XmlImportService;
import com.api.dto.ConfirmarImportacaoRequest;
import com.api.dto.ImportacaoNfeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/estoque/importar")
public class XmlImportController {

    @Autowired
    private XmlImportService xmlImportService;

    @PostMapping("/processar")
    public ResponseEntity<ImportacaoNfeResponse> processarXml(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            return ResponseEntity.ok(xmlImportService.processarXml(arquivo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmar(@RequestBody ConfirmarImportacaoRequest request) {
        xmlImportService.confirmarImportacao(request);
        return ResponseEntity.ok().build();
    }
}

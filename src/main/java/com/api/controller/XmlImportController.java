package com.api.controller;

import com.api.business.EstoqueService;
import com.api.business.XmlImportService;
import com.api.dto.xml.ConfirmarImportacaoDTO;
import com.api.dto.xml.ImportacaoResultadoDTO;
import com.api.dto.xml.ItemConferenciaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/estoque/importar")
public class XmlImportController {

    @Autowired
    private XmlImportService xmlImportService;

    @PostMapping("/processar")
    public ResponseEntity<?> importarXML(@RequestParam("arquivo") MultipartFile arquivo) {
        try {

            ImportacaoResultadoDTO resultado = xmlImportService.prepararConferencia(arquivo);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/confirmar-importacao")
    public ResponseEntity<?> confirmar(
            @RequestBody ConfirmarImportacaoDTO dto) {

        xmlImportService.salvarItensRevisados(
                dto.getItens(),
                dto.getReimportar()
        );
        return ResponseEntity.ok("Estoque atualizado");
    }
}

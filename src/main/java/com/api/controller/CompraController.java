package com.api.controller;

import com.api.business.CompraService;
import com.api.dto.CompraDTO;
import com.api.dto.CompraDetalheDTO;
import com.api.dto.CompraResumoDTO;
import com.api.dto.RecebimentoDTO;
import com.api.entity.Compra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping("/salvar")
    public ResponseEntity<Compra> salvarCompra(@RequestBody CompraDTO dto) {
        Compra novaCompra = compraService.salvarCompra(dto);
        return ResponseEntity.ok(novaCompra);
    }

    @PostMapping("/alterar/{id}")
    public ResponseEntity<Compra> salvarCompraAlterada(@PathVariable Long id, @RequestBody CompraDTO dto) {
        var response = compraService.editarCompraPorId(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<CompraResumoDTO>> listar() {
        // Certifique-se de que chama o serviço que usa a query do DTO
        List<CompraResumoDTO> lista = compraService.listarResumo();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDetalheDTO> buscarPorId(@PathVariable Long id) {
        CompraDetalheDTO detalhe = compraService.buscarDetalhesPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @DeleteMapping("/deletar/{id}/{idCompra}")
     public void deletarItemCompra(@PathVariable Long id,
                                   @PathVariable Long idCompra){
          compraService.deletarItemCompra(id ,idCompra);
    }

    @PostMapping("/{id}/previsao/{previsao}")
    public void ajustarPrevisao(@PathVariable Long id,
                                                  @PathVariable
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                  LocalDate previsao) {
        Compra response = compraService.ajustarPrevisao(id, previsao);
        //return ResponseEntity.ok().body();
    }

    @PostMapping("/{id}/receber")
    public ResponseEntity<Void> confirmarRecebimento(@PathVariable Long id, @RequestBody RecebimentoDTO notaFiscal ) throws Exception {
        compraService.processarRecebimentoEstoque(id, notaFiscal.getNumeroNotaFiscal());
        return ResponseEntity.ok().build();
    }
}
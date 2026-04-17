package com.api.controller;


import com.api.business.ProdutoFornecedorService;
import com.api.business.ProdutoService;
import com.api.business.VendaService;
import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.dto.requests.PdvRequest;
import com.api.dto.requests.ProdutoRequest;
import com.api.dto.VendaDTO;
import com.api.entity.Venda;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "X-Pdv-Status")
@RestController
@RequestMapping("/api/pdv")
public class VendaController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoFornecedorService produtoFornecedorService;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private VendaService vendaService;

    @PostMapping("/finalizar")
    public ResponseEntity<Venda> finalizar(@RequestBody VendaDTO vendaDto) {
        Venda vendaSalva = vendaService.finalizarVenda(vendaDto);
        return ResponseEntity.ok(vendaSalva);
    }

    // PORTA 1: Só para sugestões na tela
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarParaPDV(@RequestParam String termo) {
        // SE BIPAR BALANÇA: Comando de inserção automática
        if (termo.length() == 13 && termo.startsWith("2")) {
            return ResponseEntity.accepted().header("X-Action", "PROCESSAR").build();
        }

        // SE BIPAR CÓDIGO DE BARRAS FIXO (EAN13 comum): Também insere automático
        if (termo.length() == 13) {
            return ResponseEntity.accepted().header("X-Action", "PROCESSAR").build();
        }

        // SE ESTIVER APENAS DIGITANDO NOME: Retorna lista de sugestões
        return ResponseEntity.ok(produtoService.buscarFlexivel(termo));
    }

    // PORTA 2: Onde a mágica da Videira Natural acontece
    @PostMapping("/processar-item")
    public ResponseEntity<List<ItemCarrinhoRequest>> processar(@RequestBody PdvRequest request) {
        // Aqui você chama o Service que tem a regra da balança e do "UN vs KG"
        return ResponseEntity.ok(vendaService.processarAdicao(request.getTermo(), request.getCarrinho(), request.getProdutoId(), request.getPeso()));
    }
}
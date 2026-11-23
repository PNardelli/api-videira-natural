package com.api.controller;

import com.api.business.EstoqueService;
import com.api.business.ProdutoFornecedorService;
import com.api.dto.EstoqueItemDTO;
import com.api.dto.MovimentacaoDTO;
import com.api.dto.requests.PromocaoRequest;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.ProdutoFornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    @Autowired private EstoqueService estoqueService;

    @Autowired
    private ProdutoFornecedorService pfService;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @PostMapping("/movimentar")
    public ResponseEntity<?> movimentar(@RequestBody MovimentacaoDTO dto) {
        try {
            estoqueService.processarMovimentacao(dto);
            return ResponseEntity.ok("ESTOQUE ATUALIZADO!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/produto-fornecedor/{id}")
    public ProdutoFornecedor buscar(@PathVariable Long id){
        return pfService.recuperarProduto(id);
    }


    @PostMapping("/nova-entrada")
    public ResponseEntity<?> novaEntrada(@RequestBody MovimentacaoDTO dto) {
        try {
            estoqueService.processarNovaEntrada(dto);
            return ResponseEntity.ok("NOVA ENTRADA!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/listar-otimizado")
    public ResponseEntity<List<EstoqueItemDTO>> listarEstoqueOtimizado() {

        return ResponseEntity.ok(produtoFornecedorRepository.listarEstoqueCompletoOTimizado());
    }

    @PutMapping("/produto-fornecedor/{idVinculo}/promocao")
    @Transactional
    public ResponseEntity<?> ativarPromocao(
            @PathVariable Long idVinculo,
            @RequestBody PromocaoRequest request
    ) {
        ProdutoFornecedor pf = produtoFornecedorRepository.findById(idVinculo)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de produto/fornecedor não encontrado: " + idVinculo));

        boolean ativar = Boolean.TRUE.equals(request.emPromocao());
        BigDecimal valorPromo = request.valorPromocional();

        if (ativar) {
            if (valorPromo == null || valorPromo.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Informe um valor promocional maior que zero."));
            }
            pf.setEmPromocao(true);
            pf.setValorPromocional(valorPromo);
        } else {
            pf.setEmPromocao(false);
            pf.setValorPromocional(null);
        }

        ProdutoFornecedor salvo = produtoFornecedorRepository.save(pf);
        return ResponseEntity.ok(salvo);
    }
}

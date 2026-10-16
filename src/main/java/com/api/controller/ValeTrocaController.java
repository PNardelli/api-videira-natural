package com.api.controller;

import com.api.business.ValeTrocaService;
import com.api.entity.ValeTroca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vale-troca")
@CrossOrigin(origins = "*")
public class ValeTrocaController {

    @Autowired
    private ValeTrocaService valeTrocaService;

    public static class ItemDevolvidoDto {
        private Long produtoId;
        private int quantidade;

        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    }

    public static class RequisicaoValeTrocaDto {
        private Long vendaOrigemId;
        private Long clienteId;
        private BigDecimal valorOriginal;
        private int diasValidade;
        private List<ItemDevolvidoDto> itensDevolvidos;

        public Long getVendaOrigemId() { return vendaOrigemId; }
        public void setVendaOrigemId(Long vendaOrigemId) { this.vendaOrigemId = vendaOrigemId; }
        public Long getClienteId() { return clienteId; }
        public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
        public BigDecimal getValorOriginal() { return valorOriginal; }
        public void setValorOriginal(BigDecimal valorOriginal) { this.valorOriginal = valorOriginal; }
        public int getDiasValidade() { return diasValidade; }
        public void setDiasValidade(int diasValidade) { this.diasValidade = diasValidade; }
        public List<ItemDevolvidoDto> getItensDevolvidos() { return itensDevolvidos; }
        public void setItensDevolvidos(List<ItemDevolvidoDto> itensDevolvidos) { this.itensDevolvidos = itensDevolvidos; }
    }

    @PostMapping("/gerar")
    public ResponseEntity<ValeTroca> gerarValeTroca(@RequestBody RequisicaoValeTrocaDto dto) {
        try {
            ValeTroca novoVale = valeTrocaService.gerarValeTroca(
                    dto.getVendaOrigemId(),
                    dto.getClienteId(),
                    dto.getValorOriginal(),
                    dto.getDiasValidade(),
                    dto.getItensDevolvidos()
            );
            return ResponseEntity.ok(novoVale);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
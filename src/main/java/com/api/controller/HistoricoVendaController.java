package com.api.controller;

import com.api.dto.ItemVendaDTO;
import com.api.dto.PagamentoDTO;
import com.api.dto.historicoVendas.VendaHistoricoDTO;
import com.api.entity.Venda;
import com.api.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/vendas")
public class HistoricoVendaController {

    @Autowired
    private VendaRepository vendaRepository;

    @Transactional(readOnly = true)
    @GetMapping("/historico")
    public ResponseEntity<List<VendaHistoricoDTO>> listarHistorico(@RequestParam(required = false) String termo) {

        // 1. Define o período do dia atual para a busca padrão
        LocalDateTime inicioHora = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimHora = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Venda> vendas;
        if (termo != null && !termo.trim().isEmpty()) {
            vendas = vendaRepository.findByTermoOrDataVendaBetween(termo, inicioHora, fimHora);
        } else {
            vendas = vendaRepository.findByDataVendaBetweenOrderByDataVendaDesc(inicioHora, fimHora);
        }

        List<VendaHistoricoDTO> dtos = vendas.stream().map(v -> {
            VendaHistoricoDTO dto = new VendaHistoricoDTO();
            dto.setId(v.getId());

            // Data e hora formatadas corretamente a partir de dataVenda
            dto.setDataHora(v.getDataVenda() != null ? v.getDataVenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");

            dto.setClienteNome(v.getCliente() != null ? v.getCliente().getNome() : "Consumidor Final");
            dto.setSubtotal(v.getSubtotal());
            dto.setDesconto(v.getDesconto());

            // Valor total mapeado corretamente usando v.getTotal()
            dto.setTotalFinal(v.getTotal());

            // Tratamento da forma de pagamento (única ou múltipla detalhada)
            if (v.getPagamentos() != null && !v.getPagamentos().isEmpty()) {
                if (v.getPagamentos().size() == 1) {
                    var unico = v.getPagamentos().get(0);
                    dto.setMetodoPagamento(unico.getTipo() != null ? unico.getTipo().name() : "DINHEIRO");
                } else {
                    String descricaoMultipla = v.getPagamentos().stream()
                            .map(p -> p.getTipo() + " (R$ " + p.getValor() + ")")
                            .collect(Collectors.joining(" + "));
                    dto.setMetodoPagamento(descricaoMultipla);
                }
            } else {
                dto.setMetodoPagamento("DINHEIRO");
            }

            // Mapeia os itens da venda
            dto.setItens(v.getItens().stream().map(i -> {
                ItemVendaDTO itemDto = new ItemVendaDTO();
                itemDto.setProdutoId(i.getProduto() != null ? i.getProduto().getId() : null);

                String nomeProd = "Produto";
                if (i.getProduto() != null && i.getProduto().getNome() != null) {
                    nomeProd = i.getProduto().getNome();
                } else if (i.getProdutoFornecedor() != null && i.getProdutoFornecedor().getProduto() != null) {
                    nomeProd = i.getProdutoFornecedor().getProduto().getNome();
                }
                itemDto.setNome(nomeProd);

                itemDto.setQuantidade(i.getQuantidade());
                itemDto.setPrecoUnitario(i.getPrecoUnitario());
                return itemDto;
            }).collect(Collectors.toList()));

            // Mapeia os pagamentos individuais para a reimpressão
            if (v.getPagamentos() != null) {
                dto.setPagamentos(v.getPagamentos().stream().map(p -> {
                    PagamentoDTO pagDto = new PagamentoDTO();
                    pagDto.setTipo(p.getTipo());
                    pagDto.setValor(p.getValor());
                    return pagDto;
                }).collect(Collectors.toList()));
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
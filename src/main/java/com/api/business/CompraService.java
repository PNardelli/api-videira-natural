package com.api.business;

import com.api.dto.CompraDTO;
import com.api.dto.CompraDetalheDTO;
import com.api.dto.CompraResumoDTO;
import com.api.dto.ItemCompraDTO;
import com.api.entity.*;
import com.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ProdutoService produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Transactional
    public Compra salvarCompra(CompraDTO dto) {
        Fornecedor fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        Compra compra = new Compra();
        compra.setFornecedor(fornecedor);

        List<ItemCompra> itens = new ArrayList<>();
        for (ItemCompraDTO itemDto : dto.getItens()) {
            Produto produto = produtoRepository.buscar(itemDto.getProdutoId());
            ProdutoFornecedor produtoFornecedor = estoqueRepository.recuperarMaiorValorCompra(itemDto.getProdutoId());

            ItemCompra item = new ItemCompra();
            item.setCompra(compra);
            item.setProdutoFornecedor(produtoFornecedor);
            item.setQuantidade(itemDto.getQuantidade());
            item.setValorUnitario(produtoFornecedor.getPrecoCompra() != null ? produtoFornecedor.getPrecoCompra().doubleValue() : 0.0);
            itens.add(item);
        }

        compra.setItens(itens);
        return compraRepository.save(compra);
    }

    @Transactional(readOnly = true) // <-- Garante sessão aberta para a projeção do DTO
    public List<CompraResumoDTO> listarResumo() {
        return compraRepository.listarResumoCompras();
    }

    @Transactional(readOnly = true)
    public CompraDetalheDTO buscarDetalhesPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        // Converte os itens da entidade para a lista de ItemCompraDTO
        List<ItemCompraDTO> itensDto = compra.getItens().stream().map(item ->
                new ItemCompraDTO(
                        item.getId(),// id
                        item.getProdutoFornecedor().getProduto().getId() != null ? item.getProdutoFornecedor().getProduto().getId() : null,   // produtoId
                        item.getProdutoFornecedor().getProduto() != null ? item.getProdutoFornecedor().getProduto().getNome() : "Produto", // nomeProduto
                        item.getProdutoFornecedor().getProduto() != null ? item.getProdutoFornecedor().getProduto().getUnidadeMedida().toString() : "UN", // unidadeMedida
                        item.getQuantidade(),                                           // quantidade
                        item.getValorUnitario(),
                        item.getValorUnitario()

                )
        ).collect(Collectors.toList());

        // Retorna o DTO de detalhes completo preenchendo o cabeçalho e os itens
        return new CompraDetalheDTO(
                compra.getId(),
                compra.getDataHora(),
                compra.getStatus(),
                compra.getPrevisaoEntrega(),
                compra.getDataEntrega(),
                compra.getFornecedor() != null ? compra.getFornecedor().getId() : null,
                compra.getFornecedor() != null ? compra.getFornecedor().getNome() : "Não informado",
                itensDto
        );
    }

    @Transactional(readOnly = true)
    public Compra ajustarPrevisao(@PathVariable Long id, LocalDate previsao) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));
        compra.setDataEntrega(previsao);
        return ResponseEntity.ok(compra).getBody();
    }


        @Transactional
    public void processarRecebimentoEstoque(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        // Atualiza o status para recebida e define a data de entrega atual
        compra.setStatus("RECEBIDA");
        compra.setDataEntrega(LocalDate.now());

        // Se você tiver a lógica de somar os itens ao estoque dos produtos, chame aqui:
        // for (ItemCompra item : compra.getItens()) {
        //     produtoService.adicionarEstoque(item.getProduto().getId(), item.getQuantidadePedida());
        // }

        compraRepository.save(compra);
    }
}
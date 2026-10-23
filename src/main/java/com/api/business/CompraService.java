package com.api.business;

import com.api.dto.CompraDTO;
import com.api.dto.ItemCompraDTO;
import com.api.entity.Compra;
import com.api.entity.Fornecedor;
import com.api.entity.ItemCompra;
import com.api.entity.Produto;
import com.api.repository.CompraRepository;
import com.api.repository.FornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
    public Compra salvarCompra(CompraDTO dto) {
        Fornecedor fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        Compra compra = new Compra();
        compra.setFornecedor(fornecedor);

        List<ItemCompra> itens = new ArrayList<>();
        for (ItemCompraDTO itemDto : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDto.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado ID: " + itemDto.getProdutoId()));

            ItemCompra item = new ItemCompra();
            item.setCompra(compra);
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setValorUnitario(itemDto.getValorUnitario() != null ? itemDto.getValorUnitario() : 0.0);

            itens.add(item);
        }

        compra.setItens(itens);
        return compraRepository.save(compra);
    }
}
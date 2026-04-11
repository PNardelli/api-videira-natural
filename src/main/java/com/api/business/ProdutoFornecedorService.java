package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.FornecedorRepository;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProdutoFornecedorService {

    @Autowired
    ProdutoFornecedorRepository repositoryPF;

    @Autowired
    FornecedorRepository fornecedorRepository;

    public ProdutoFornecedor salvar(ProdutoFornecedorDTO produtoFornecedorDTO){
        ProdutoFornecedor produtoFornecedor = new ProdutoFornecedor();
        return repositoryPF.save(produtoFornecedor);
    }

    public ProdutoFornecedor cadastrarProdutoFornecedor(Produto produto, Long idFornecedor){

        ProdutoFornecedor produtoFornecedor = new ProdutoFornecedor();
        produtoFornecedor.setProduto(produto);

        if (idFornecedor != null){
            Fornecedor fornecedor = fornecedorRepository.findById(idFornecedor).orElseThrow();
            produtoFornecedor.setFornecedor(fornecedor);
        }

        produtoFornecedor.setQuantidade(produto.getEstoque());
        produtoFornecedor.setUnidade(produto.getUnidadeMedida().toString());
        produtoFornecedor.setDataValidade(produto.getDataValidade());
        produtoFornecedor.setDataEntrada(LocalDateTime.now());
        produtoFornecedor.setPrecoCompra(produto.getPrecoCompra());
        produtoFornecedor.setPrecoVenda(produto.getPrecoVenda());

        return repositoryPF.save(produtoFornecedor);
    }

    public ProdutoFornecedor recuperarProduto(Long id) {
         ProdutoFornecedor response = repositoryPF.findById(id).orElseThrow();
         return response;
    }
}

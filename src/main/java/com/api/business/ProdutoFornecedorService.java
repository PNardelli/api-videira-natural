package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProdutoFornecedorService {

    @Autowired
    ProdutoFornecedorRepository repositoryPF;

    public ProdutoFornecedor salvar(ProdutoFornecedorDTO produtoFornecedorDTO){
        ProdutoFornecedor produtoFornecedor = new ProdutoFornecedor();


        return repositoryPF.save(produtoFornecedor);
    }

    public ProdutoFornecedor cadastrarProdutoFornecedor(Produto produto){
        ProdutoFornecedor produtoFornecedor = new ProdutoFornecedor();
        produtoFornecedor.setProduto(produto);

        produtoFornecedor.setQuantidade(0);
        produtoFornecedor.setUnidade(produto.getUnidadeMedida().toString());
        produtoFornecedor.setDataValidade(produtoFornecedor.getDataValidade());
        produtoFornecedor.setDataEntrada(LocalDateTime.now());
        produtoFornecedor.setPrecoCompra(produto.getPrecoCompra());
        produtoFornecedor.setPrecoVenda(produto.getPrecoVenda());

        return repositoryPF.save(produtoFornecedor);
    }

}

package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ProdutoFornecedorService {

    @Autowired
    ProdutoFornecedorRepository repositoryPF;

    public ProdutoFornecedor salvar(ProdutoFornecedorDTO produtoFornecedorDTO){
        ProdutoFornecedor produtoFornecedor = new ProdutoFornecedor();

        return repositoryPF.save(produtoFornecedor);
    }

}

package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.requests.ProdutoRequest;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.FornecedorRepository;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProdutoFornecedorService {

    @Autowired
    ProdutoFornecedorRepository repositoryPF;

    @Autowired
    FornecedorRepository fornecedorRepository;

    @Autowired
    ProdutoService produtoService;

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

    public ProdutoRequest recuperarProdutoGranel(String codigoBarra){
        String produtoNoCodigoBarra = codigoBarra.substring(1,5);
        String valorNoCodigoBarra = codigoBarra.substring(8,12);

        ProdutoRequest produtoRequest = new ProdutoRequest();

        List<ProdutoFornecedorDTO> produto = produtoService.buscarFlexivel(produtoNoCodigoBarra);

        ProdutoFornecedor produtoFornecedor = repositoryPF.getProdutoFornecedor(produto.get(0).getProdutoId());



        BigDecimal valorConvertido = new BigDecimal(valorNoCodigoBarra).movePointLeft(2);
        BigDecimal pesoComprado = valorConvertido.divide(
                produtoFornecedor.getPrecoVenda(),
                3,
                RoundingMode.HALF_UP
        );
        produtoRequest.setProdutoId(produtoFornecedor.getProduto().getId());
        produtoRequest.setCodigoProduto(produtoFornecedor.getProduto().getCodigoProduto());
        produtoRequest.setEstoque(pesoComprado);
        produtoRequest.setPrecoVenda(produtoFornecedor.getPrecoVenda());
        produtoRequest.setNome(produtoFornecedor.getProduto().getNome());
        produtoRequest.setUnidadeMedida(produtoFornecedor.getProduto().getUnidadeMedida());

        return produtoRequest;
    }
}

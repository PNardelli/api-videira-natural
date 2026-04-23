package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.requests.ProdutoRequest;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.FornecedorRepository;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
        produtoFornecedor.setProdutoAtivo(true);

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

        List<ProdutoFornecedor> produtoFornecedorList = repositoryPF.getProdutoFornecedorList(produto.get(0).getProdutoId(), PageRequest.of(0,1));
        //ProdutoFornecedor produtoFornecedor = repositoryPF.getProdutoFornecedor(produto.get(0).getProdutoId());



        BigDecimal valorConvertido = new BigDecimal(valorNoCodigoBarra).movePointLeft(2);
        BigDecimal pesoComprado = valorConvertido.divide(
                produtoFornecedorList.get(0).getPrecoVenda(),
                3,
                RoundingMode.HALF_UP
        );
        produtoRequest.setProdutoId(produtoFornecedorList.get(0).getProduto().getId());
        produtoRequest.setCodigoProduto(produtoFornecedorList.get(0).getProduto().getCodigoProduto());
        produtoRequest.setEstoque(pesoComprado);
        produtoRequest.setPrecoVenda(produtoFornecedorList.get(0).getPrecoVenda());
        produtoRequest.setNome(produtoFornecedorList.get(0).getProduto().getNome());
        produtoRequest.setUnidadeMedida(produtoFornecedorList.get(0).getProduto().getUnidadeMedida());

        return produtoRequest;
    }
}

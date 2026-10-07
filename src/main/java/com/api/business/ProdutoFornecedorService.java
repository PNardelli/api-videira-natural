package com.api.business;

import com.api.dto.EstoqueItemDTO;
import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.requests.ProdutoRequest;
import com.api.eNum.UnidadeMedida;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.FornecedorRepository;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

    public ProdutoFornecedor cadastrarProdutoFornecedor(Produto produto, ProdutoRequest produtoRequest, Long idFornecedor){

        ProdutoFornecedor produtoFornecedor = new ProdutoFornecedor();
        produtoFornecedor.setProduto(produto);

        if (idFornecedor != null){
            Fornecedor fornecedor = fornecedorRepository.findById(idFornecedor).orElseThrow();
            produtoFornecedor.setFornecedor(fornecedor);
        }


        produtoFornecedor.setQuantidade(produtoRequest.getEstoque());
        produtoFornecedor.setEstoqueMinimo(produtoRequest.getEstoqueMinimo());
        produtoFornecedor.setDataValidade(produtoRequest.getDataValidade());
        produtoFornecedor.setDataValidadeDias(produtoRequest.getDataValidadeDias().toString());
        produtoFornecedor.setPrecoVenda(produtoRequest.getPrecoVenda());
        produtoFornecedor.setPrecoCompra(produtoRequest.getPrecoCompra());
        produtoFornecedor.setUnidadeMedida(produto.getUnidadeMedida().toString());
        produtoFornecedor.setDataEntrada(LocalDateTime.now());
        produtoFornecedor.setProdutoAtivo(true);

        return repositoryPF.save(produtoFornecedor);
    }

    public ProdutoFornecedor recuperarProduto(Long id) {
         ProdutoFornecedor response = repositoryPF.findById(id).orElseThrow();
         return response;
    }

    public List<EstoqueItemDTO> listarEstoqueCompletoOtimizado(){
        return repositoryPF.listarEstoqueCompletoOTimizado();
    };

    public ProdutoRequest recuperarProdutoGranel(String codigoBarra, Pageable pageable){

        //Recupera em String, converte para numero para remover os 0 a esqueda e reconverte em String.
        String produtoNoCodigoBarra = codigoBarra.substring(1,6);
        Long idOuCodigoProduto = Long.parseLong(produtoNoCodigoBarra);


        String valorNoCodigoBarra = codigoBarra.substring(8,12);

        ProdutoRequest produtoRequest = new ProdutoRequest();

        List<ProdutoFornecedorDTO> produtoUnico = repositoryPF.buscarGranelPorCodigoLoja(idOuCodigoProduto.toString());

        BigDecimal valorConvertido = new BigDecimal(valorNoCodigoBarra).movePointLeft(2);
        BigDecimal pesoComprado = valorConvertido.divide(
                produtoUnico.get(0).getPrecoVenda(),
                3,
                RoundingMode.HALF_UP
        );
        produtoRequest.setProdutoId(produtoUnico.get(0).getProdutoId());
        produtoRequest.setCodigoProdutoCodigoBarras(produtoUnico.get(0).getCodigoProduto());
        produtoRequest.setEstoque(pesoComprado);
        produtoRequest.setPrecoVenda(produtoUnico.get(0).getPrecoVenda());
        produtoRequest.setNome(produtoUnico.get(0).getNome());
        produtoRequest.setUnidadeMedida(UnidadeMedida.KG);
        produtoRequest.setEstoqueMinimo(produtoUnico.get(0).getEstoqueMinimo());

        return produtoRequest;
    }
}

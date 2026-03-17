package com.api.business;

import com.api.dto.ProdutoRequest;
import com.api.entity.Categoria;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.CategoriaRepository;
import com.api.repository.FornecedorRepository;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    public Produto salvar(ProdutoRequest req) {

        Categoria categoria = categoriaRepository
                .findById(req.getCategoriaId())
                .orElseThrow();

        Produto produto = new Produto();
        produto.setCodigoProduto(req.getCodigoProduto());
        produto.setNome(req.getNome());
        produto.setCategoria(categoria);
        produto.setPrecoVenda(req.getPrecoVenda());
        produto.setUnidadeMedida(req.getUnidadeMedida());
        produto.setDataValidade(req.getDataValidade());
        produto.setDataCriacao(LocalDate.now().toString());
        if (req.getObservacao().isBlank()){
            produto.setObservacao("Sem Observação Cadastrada");
        }else {produto.setObservacao(req.getObservacao());}

        if (req.getFornecedorId() != null) {
            Fornecedor fornecedor = fornecedorRepository.findById(req.getFornecedorId())
                    .orElseThrow(() -> new RuntimeException("Fornecedor não cadastrado"));

            // Cria o vínculo
            ProdutoFornecedor vinculo = new ProdutoFornecedor();
            vinculo.setProduto(produto);
            vinculo.setFornecedor(fornecedor);
            vinculo.setPrecoCompra(req.getPrecoCompra());
            vinculo.setDataValidade(req.getDataValidade());
            vinculo.setQuantidade(req.getEstoque());
            vinculo.setUnidade(req.getUnidadeMedida().toString());

            // Adiciona na lista do produto (o Cascade salvará isso)
            produto.setFornecedores(List.of(vinculo));
        }

        return produtoRepository.save(produto);
    }

    public List<Produto> listar(){
        return produtoRepository.findAll();
    }

    public Produto buscar(Long id){
        return produtoRepository.findById(id).orElse(null);
    }

    public void deletar(Long id){
        produtoRepository.deleteById(id);
    }

}
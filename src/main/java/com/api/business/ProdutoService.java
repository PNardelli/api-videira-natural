package com.api.business;

import com.api.dto.ProdutoRequest;
import com.api.entity.*;
import com.api.repository.CategoriaRepository;
import com.api.repository.FornecedorRepository;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
        produto.setDescricao(req.getDescricao().isBlank() ? "Descrição não Cadastrada" : req.getDescricao());
        produto.setCategoria(categoria);
        produto.setPrecoVenda(req.getPrecoVenda());
        produto.setPrecoCompra(req.getPrecoCompra());
        produto.setUnidadeMedida(req.getUnidadeMedida());
        produto.setDataValidade(adicionarDataValidadeDias(req.getDataValidadeDias()));
        produto.setDataCriacao(LocalDate.now());
        produto.setObservacao(req.getObservacao().isBlank() ? "Sem Observação Cadastrada" : req.getObservacao());

        return produtoRepository.save(produto);
    }

    public List<Produto> listar(){
        return produtoRepository.findAll();
    }

    public Produto buscar(Long id){
        return produtoRepository.findById(id).orElse(null);
    }

    public Produto atualizarProduto(Long id, ProdutoRequest produtoAtualizado) {
        Produto produtoExistente = produtoRepository.findById(id).orElseThrow();

        produtoExistente.setCodigoProduto(produtoAtualizado.getCodigoProduto());
        produtoExistente.setDataValidade(produtoAtualizado.getDataValidade());

        return produtoRepository.save(produtoExistente);
    }

    public void deletar(Long id){
        produtoRepository.deleteById(id);
    }

    public void ajustarEstoque(Long id, Map<String, Object> dados){

    }


  private LocalDate adicionarDataValidadeDias(Long data){
      return (data != null) ? LocalDate.now().plusDays(data) : LocalDate.now();
  }

}
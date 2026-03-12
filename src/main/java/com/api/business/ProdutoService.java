package com.api.business;

import com.api.dto.ProdutoRequest;
import com.api.entity.Categoria;
import com.api.entity.Produto;
import com.api.repository.CategoriaRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

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
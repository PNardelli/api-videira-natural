package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.requests.ProdutoRequest;
import com.api.entity.*;
import com.api.repository.CategoriaRepository;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Produto salvar(ProdutoRequest req) {

        Categoria categoria = categoriaRepository
                .findById(req.getCategoriaId())
                .orElseThrow();

        Produto produto = new Produto();

        produto.setCodigoBarras(
                (req.getCodigoBarra() == null || req.getCodigoBarra().isBlank())
                        ? req.getCodigoProduto().trim().toUpperCase()
                        : req.getCodigoBarra()
        );
        produto.setCodigoProduto(req.getCodigoProduto().isEmpty() || req.getCodigoProduto().isBlank() ? req.getCodigoBarra().trim().toUpperCase() : req.getCodigoProduto().trim().toUpperCase());
        produto.setNome(req.getNome().trim().toUpperCase());
        produto.setDescricao(
                (req.getDescricao() == null || req.getDescricao().isBlank())
                        ? "DESCRIÇÃO NÃO CADASTRADA"
                        : req.getDescricao().trim().toUpperCase()
        );        produto.setCategoria(categoria);
        produto.setPrecoVenda(req.getPrecoVenda());
        produto.setPrecoCompra(req.getPrecoCompra());
        produto.setUnidadeMedida(req.getUnidadeMedida());
        produto.setEstoque(req.getEstoque());
        produto.setDataValidade(req.getDataValidade());
        produto.setDataCriacao(LocalDate.now());
        produto.setObservacao(
                (req.getObservacao() == null || req.getObservacao().isBlank())
                        ? "OBSERVAÇÃO NÃO CADASTRADA"
                        : req.getObservacao().trim().toUpperCase()
        );
        if (req.getCodigoFornecedorXml() != null){
            produto.setCodigoFornecedorXml(req.getCodigoFornecedorXml().toString());
        }

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

        Optional<Categoria> categoriaAtualizada = categoriaRepository.findById(produtoAtualizado.getCategoriaId());

        produtoExistente.setCodigoProduto(produtoAtualizado.getCodigoProduto().trim().toUpperCase());
        produtoExistente.setCodigoBarras(produtoAtualizado.getCodigoBarra().trim().toUpperCase());
        produtoExistente.setDataValidade(produtoAtualizado.getDataValidade());
        produtoExistente.setObservacao(produtoAtualizado.getObservacao().isEmpty() || produtoAtualizado.getObservacao().isBlank() ? produtoExistente.getObservacao().trim().toUpperCase() : produtoExistente.getObservacao().trim().toUpperCase());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao().trim().toUpperCase());
        produtoExistente.setCategoria(categoriaAtualizada.orElseThrow());
        produtoExistente.setPrecoCompra(produtoAtualizado.getPrecoCompra());
        produtoExistente.setPrecoVenda(produtoAtualizado.getPrecoVenda());
        produtoExistente.setUnidadeMedida(produtoAtualizado.getUnidadeMedida());

        return produtoRepository.save(produtoExistente);
    }

    public void deletar(Long id){
        produtoRepository.deleteById(id);
    }


  private LocalDate adicionarDataValidadeDias(Long data){
      return (data != null) ? LocalDate.now().plusDays(data) : LocalDate.now();
  }

    public List<ProdutoFornecedorDTO> buscarFlexivel(String termo) {
        List<Produto> produtos = produtoRepository.findByFlexivel(termo);



        return produtos.stream().map(p -> {
            ProdutoFornecedorDTO dto = new ProdutoFornecedorDTO();
            dto.setProdutoId(p.getId());
            dto.setNome(p.getNome());
            dto.setPrecoVenda(p.getPrecoVenda());
            dto.setUnidade(String.valueOf(p.getUnidadeMedida()));
            return dto;
        }).collect(Collectors.toList());
    }
}
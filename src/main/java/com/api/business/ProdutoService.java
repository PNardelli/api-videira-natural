package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.requests.ProdutoRequest;
import com.api.entity.Categoria;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import com.api.repository.CategoriaRepository;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Produto salvar(ProdutoRequest req) {

        Categoria categoria = categoriaRepository
                .findById(req.getCategoriaId())
                .orElseThrow();

        Produto produto = new Produto();

        if (req.getCodigoProdutoCodigoBarras().length() <= 6){
            produto.setCodigoProduto(req.getCodigoProdutoCodigoBarras());
        }else{
            produto.setCodigoBarras(req.getCodigoProdutoCodigoBarras());
        }

        produto.setNome(req.getNome().trim().toUpperCase());
        produto.setDescricao(
                (req.getDescricao() == null || req.getDescricao().isBlank())
                        ? "DESCRIÇÃO NÃO CADASTRADA"
                        : req.getDescricao().trim().toUpperCase()
        );        produto.setCategoria(categoria);

        produto.setUnidadeMedida(req.getUnidadeMedida());
        produto.setDataCriacao(LocalDate.now());
        produto.setUltimaAtualizacao(LocalDate.now());
        produto.setPrecoVenda(req.getPrecoVenda());
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

    public Page<Produto> listar(Pageable pageable) {
        Page<Produto> paginaProdutos = produtoRepository.findAll(pageable);

        // 🔍 Debug no console do Backend Java
        System.out.println("=== DEBUG SPRING PAGE ===");
        System.out.println("Total de elementos na base: " + paginaProdutos.getTotalElements());
        System.out.println("Total de páginas: " + paginaProdutos.getTotalPages());
        System.out.println("Elementos nesta página (content size): " + paginaProdutos.getContent().size());
        System.out.println("Primeiro produto: " + (paginaProdutos.getContent().isEmpty() ? "Vazio" : paginaProdutos.getContent().get(0).getNome()));

        return paginaProdutos;
    }

    public Page<Produto> listarOuBuscar(String termo, Long categoriaId, Pageable pageable) {
        // Se passou a categoria e também um termo de busca
        if (categoriaId != null && termo != null && !termo.isBlank()) {
            return produtoRepository.findByCategoriaAndFlexivelPaginado(categoriaId, termo.trim(), pageable);
        }
        // Se passou apenas a categoria
        if (categoriaId != null) {
            return produtoRepository.findByCategoriaId(categoriaId, pageable);
        }
        // Se passou apenas o termo de busca
        if (termo != null && !termo.isBlank()) {
            return produtoRepository.findByFlexivelPaginado(termo.trim(), pageable);
        }
        // Se não passou nada, traz tudo paginado
        return produtoRepository.findAll(pageable);
    }

    public Page<Produto> listarPaginado(Pageable pageable) {
        return produtoRepository.findAll(pageable);
    }

    public Produto buscar(Long id){
        return produtoRepository.findById(id).orElse(null);
    }

    public Produto atualizarProduto(Long id, ProdutoRequest produtoAtualizado) {
        Produto produtoExistente = produtoRepository.findById(id).orElseThrow();

        if (produtoAtualizado.getCategoriaId() != null){
            Optional<Categoria> categoriaAtualizada = categoriaRepository.findById(produtoAtualizado.getCategoriaId());
            produtoExistente.setCategoria(categoriaAtualizada.orElseThrow());
        }

        if (produtoAtualizado.getCodigoProdutoCodigoBarras().length() <= 6){
            produtoExistente.setCodigoBarras(null);
            produtoExistente.setCodigoProduto(produtoAtualizado.getCodigoProdutoCodigoBarras());
        }else{
            produtoExistente.setCodigoProduto(null);
            produtoExistente.setCodigoBarras(produtoAtualizado.getCodigoProdutoCodigoBarras());
        }
        produtoExistente.setNome(produtoAtualizado.getNome().toUpperCase());
        produtoExistente.setUnidadeMedida(produtoAtualizado.getUnidadeMedida());
        produtoExistente.setPrecoVenda(produtoAtualizado.getPrecoVenda());
        produtoExistente.setUltimaAtualizacao(LocalDate.now());

        return produtoRepository.save(produtoExistente);
    }

    public void deletar(Long id){
        produtoRepository.deleteById(id);
    }

    public Produto ativarInativar(Long id){
        Produto produto =  produtoRepository.findById(id).orElseThrow();

        if (produto.isAtivo()){
            produto.setAtivo(false);
        }else{
            produto.setAtivo(true);
        };

        return produtoRepository.save(produto);
    }


  private LocalDate adicionarDataValidadeDias(Long data){
      return (data != null) ? LocalDate.now().plusDays(data) : LocalDate.now();
  }

    public List<ProdutoFornecedorDTO> buscarFlexivel(String termo) {
        List<Produto> produtos = produtoRepository.findByFlexivel(termo);
        return produtos.stream().map(p -> {
            ProdutoFornecedorDTO dto = new ProdutoFornecedorDTO();
            ProdutoFornecedor produtoFornecedor = produtoFornecedorRepository.getProdutoFornecedor(p.getId());
            BigDecimal estoque = produtoFornecedor.getQuantidade();

            if (p.getCodigoProduto() == null){
                dto.setCodigoProduto(p.getCodigoBarras());
            }else{
                dto.setCodigoProduto(p.getCodigoProduto());
            }
            dto.setQuantidade(estoque);
            dto.setProdutoId(p.getId());
            dto.setNome(p.getNome());
           // dto.setPrecoVenda(p.getPrecoVenda());
            dto.setUnidade(String.valueOf(p.getUnidadeMedida()));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ProdutoFornecedor> listarEstoqueCriticoOtimizado() {
        return produtoFornecedorRepository.findEstoqueCritico();
    }

    public void atualizarPreco(Long id, BigDecimal preco){
        Produto produto = produtoRepository.findById(id).orElseThrow();
        produto.setPrecoVenda(preco);
        produtoRepository.save(produto);
    }
}
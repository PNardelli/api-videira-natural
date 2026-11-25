
package com.api.controller.mobile;

import com.api.business.ConfiguracaoFreteService;
import com.api.business.EstoqueService;
import com.api.dto.ClientePdvDTO;
import com.api.dto.requests.PedidoRequest;
import com.api.eNum.TipoVenda;
import com.api.entity.Categoria;
import com.api.entity.ProdutoFornecedor;
import com.api.entity.Promocao;
import com.api.repository.CategoriaRepository;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.PromocaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loja")
@CrossOrigin(origins = "*")
public class LojaVirtualController {

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private ConfiguracaoFreteService freteService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PromocaoRepository promocaoRepository;

    @GetMapping("/home")
    @Cacheable("vitrineCache")
    public ResponseEntity<Map<String, Object>> obterVitrine() {

        // Busca produtos ativos no estoque
        List<ProdutoFornecedor> itensEstoque =
                produtoFornecedorRepository.buscarComEstoqueEAtivosApp(0);

        List<Categoria> categorias = categoriaRepository.findAll();

        Collator collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        collator.setStrength(Collator.PRIMARY);

        List<Map<String, Object>> produtosDto = itensEstoque.stream()
                .map(pf -> {

                    Map<String, Object> map = new HashMap<>();

                    var produto = pf.getProduto();

                    if (produto != null) {
                        map.put("id", produto.getId());
                        map.put("nome", produto.getNome());

                        map.put("categoriaId",
                                produto.getCategoria() != null
                                        ? produto.getCategoria().getId()
                                        : null);

                        map.put("categoriaNome",
                                produto.getCategoria() != null
                                        ? produto.getCategoria().getNome()
                                        : "Geral");

                        map.put("sub",
                                produto.getUnidadeMedida() != null
                                        ? produto.getUnidadeMedida().toString()
                                        : "50g");

                    } else {
                        map.put("id", pf.getId());
                        map.put("nome", "Produto");
                        map.put("categoriaId", null);
                        map.put("categoriaNome", "Geral");
                        map.put("sub", "50g");
                    }

                    boolean emPromocao = Boolean.TRUE.equals(pf.getEmPromocao())
                            && pf.getValorPromocional() != null
                            && pf.getValorPromocional().compareTo(BigDecimal.ZERO) > 0;

                    map.put("produtoFornecedorId", pf.getId());
                    map.put("preco", pf.getPrecoVenda());
                    map.put("precoOriginal", pf.getPrecoVenda());
                    map.put("precoBase", pf.getPrecoVenda());
                    map.put("emPromocao", emPromocao);
                    map.put("valorPromocional", emPromocao ? pf.getValorPromocional() : null);
                    map.put("estoqueDisponivel", pf.getQuantidade());

                    map.put("img",
                            "https://images.unsplash.com/photo-1587049352847-4d4b1263d596?q=80&w=300&auto=format&fit=crop");

                    return map;

                }).sorted(Comparator.comparing(m -> Objects.toString(m.get("nome"), "Produto"),
                        collator))
                .collect(Collectors.toList());

        List<Map<String, Object>> categoriasDto = categorias.stream()
                .map(c -> {

                    Map<String, Object> map = new HashMap<>();

                    map.put("id", c.getId());
                    map.put("nome", c.getNome());

                    map.put("img",
                            "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?q=80&w=200&auto=format&fit=crop");

                    return map;

                }).sorted(Comparator.comparing(
                m -> Objects.toString(m.get("nome"), ""),
                collator
        ))
                .collect(Collectors.toList());

        Map<String, Object> resposta = new HashMap<>();

        resposta.put("categorias", categoriasDto);
        resposta.put("produtos", produtosDto);

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/calcular-frete")
    public BigDecimal calcularFrete(@RequestBody ClientePdvDTO cliente) throws UnsupportedEncodingException {
        BigDecimal frete = freteService.calcularFreteParaCliente(cliente, TipoVenda.APP);
        return frete;
    }

    @GetMapping("/limpar-cache")
    @CacheEvict(value = "vitrineCache", allEntries = true)
    public ResponseEntity<String> limparCache() {
        return ResponseEntity.ok("Cache da vitrine limpo com sucesso!");
    }
}

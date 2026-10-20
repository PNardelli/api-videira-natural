package com.api.controller;

import com.api.entity.Categoria;
import com.api.entity.Produto;
import com.api.repository.CategoriaRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loja")
public class LojaVirtualController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // A resposta deste método fica guardada em cache para ser ultra rápida
    @GetMapping("/vitrine")
    @Cacheable("vitrineCache")
    public ResponseEntity<Map<String, Object>> obterVitrine() {
        List<Produto> produtos = produtoRepository.findAll();
        List<Categoria> categorias = categoriaRepository.findAll();

        List<Map<String, Object>> produtosDto = produtos.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("nome", p.getNome());
            map.put("sub", p.getUnidadeMedida() != null ? p.getUnidadeMedida().toString() : "Unidade");
            map.put("preco", p.getPrecoVenda());
            map.put("img", "https://images.unsplash.com/photo-1587049352847-4d4b1263d596?q=80&w=300&auto=format&fit=crop");
            map.put("animando", false);
            return map;
        }).collect(Collectors.toList());

        List<Map<String, Object>> categoriasDto = categorias.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("nome", c.getNome());
            map.put("img", "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?q=80&w=200&auto=format&fit=crop");
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("categorias", categoriasDto);
        resposta.put("produtos", produtosDto);

        return ResponseEntity.ok(resposta);
    }

    // Opcional: Se quiser limpar o cache manualmente ou via rotina para atualizar os dados
    @GetMapping("/limpar-cache")
    @CacheEvict(value = "vitrineCache", allEntries = true)
    public ResponseEntity<String> limparCache() {
        return ResponseEntity.ok("Cache da vitrine limpo com sucesso!");
    }
}
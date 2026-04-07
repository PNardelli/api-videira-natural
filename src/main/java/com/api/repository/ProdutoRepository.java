package com.api.repository;

import com.api.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long > {
    boolean existsByCodigoProduto(String codigoProduto);

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    List<Produto> findByCodigoProdutoOrNomeContainingIgnoreCase(String codigo, String nome);

}

package com.api.repository;

import com.api.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long > {
    boolean existsByCodigoProduto(String codigoProduto);

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    Optional<Produto> findByCodigoFornecedorXml(String codigoFornecedor);

    List<Produto> findByCodigoProdutoOrNomeContainingIgnoreCase(String codigo, String nome);

    @Query("SELECT p FROM Produto p WHERE " +
            "CAST(p.id AS string) = :termo OR " +
            "p.codigoBarras = :termo OR " +
            "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Produto> findByFlexivel(@Param("termo") String termo);

}

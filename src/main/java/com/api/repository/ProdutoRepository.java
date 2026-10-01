package com.api.repository;

import com.api.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            "p.codigoProduto = :termo OR " +
            "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Produto> findByFlexivel(@Param("termo") String termo);

    //NOVO: Método padrão paginado para listar os produtos por blocos (ex: 10 em 10)
    Page<Produto> findAll(Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE " +
            "CAST(p.id AS string) = :termo OR " +
            "p.codigoBarras = :termo OR " +
            "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<Produto> findByFlexivelPaginado(@Param("termo") String termo, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.categoria.id = :categoriaId")
    Page<Produto> findByCategoriaId(@Param("categoriaId") Long categoriaId, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.categoria.id = :categoriaId AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR p.codigoProduto LIKE CONCAT('%', :termo, '%'))")
    Page<Produto> findByCategoriaAndFlexivelPaginado(@Param("categoriaId") Long categoriaId, @Param("termo") String termo, Pageable pageable);
}
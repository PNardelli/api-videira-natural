package com.api.repository;

import com.api.entity.ProdutoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueRepository extends JpaRepository<ProdutoFornecedor, Long> {

    @Query("""
        SELECT pf FROM ProdutoFornecedor pf 
        WHERE pf.produto.id = :produtoId 
          AND pf.fornecedor.id = :fornecedorId 
        ORDER BY pf.dataValidade ASC
        """)
    List<ProdutoFornecedor> recuperarProdutoFornecedorLista(
            @Param("produtoId") Long produtoId,
            @Param("fornecedorId") Long fornecedorId);

    @Query("""
    SELECT pf FROM ProdutoFornecedor pf 
    WHERE pf.produto.id = :produtoId 
    ORDER BY pf.precoCompra DESC
    """)
    ProdutoFornecedor recuperarMaiorValorCompra(@Param("produtoId") Long produtoId);

}

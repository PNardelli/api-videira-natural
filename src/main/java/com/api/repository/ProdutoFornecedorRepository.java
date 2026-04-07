package com.api.repository;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedor, Long> {

    @Query("SELECT pf FROM ProdutoFornecedor pf WHERE produto.id = :produtoId")
    ProdutoFornecedor getProdutoFornecedor(@Param("produtoId") Long produtoId);

    Optional<ProdutoFornecedor> findByProdutoAndFornecedor(Produto produto, Fornecedor fornecedor);
}



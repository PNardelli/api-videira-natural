package com.api.repository;

import com.api.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long > {
    boolean existsByCodigoProduto(String codigoProduto);

}

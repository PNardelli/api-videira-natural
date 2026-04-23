package com.api.repository;

import com.api.dto.AlertaEstoqueDTO;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedor, Long> {

    @Query("SELECT pf FROM ProdutoFornecedor pf WHERE produto.id = :produtoId")
    ProdutoFornecedor getProdutoFornecedor(@Param("produtoId") Long produtoId);

    Optional<ProdutoFornecedor> findByProdutoAndFornecedor(Produto produto, Fornecedor fornecedor);

    @Query("""
            SELECT pf FROM ProdutoFornecedor pf 
            WHERE pf.produto.id = :produtoId 
            ORDER BY pf.dataValidade ASC
            """)
    List<ProdutoFornecedor> getProdutoFornecedorList(Long produtoId, Pageable pageable);

    @Query("SELECT pf FROM ProdutoFornecedor pf JOIN FETCH pf.produto " +
            "WHERE pf.produto.id = :produtoId AND pf.quantidade > :qtd " +
            "ORDER BY pf.dataValidade ASC")
    List<ProdutoFornecedor> buscarLotesDisponiveis(
            @Param("produtoId") Long produtoId,
            @Param("qtd") BigDecimal qtd
    );


    @Query("SELECT pf FROM ProdutoFornecedor pf JOIN FETCH pf.produto p " +
            "WHERE p.id = :produtoId AND pf.produtoAtivo = true AND pf.quantidade > 0 " +
            "ORDER BY pf.dataValidade ASC")
    List<ProdutoFornecedor> findAtivosPorProduto(@Param("produtoId") Long produtoId);


    //PARA DASHBOARD
    @Query("SELECT new com.api.dto.AlertaEstoqueDTO(p.nome, pf.quantidade, pf.dataValidade) " +
            "FROM ProdutoFornecedor pf JOIN pf.produto p " +
            "WHERE pf.dataValidade <= :dataLimite AND pf.quantidade > 0 AND pf.produtoAtivo = true")
    List<AlertaEstoqueDTO> buscarProdutosVencendo(@Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT new com.api.dto.AlertaEstoqueDTO(p.nome, pf.quantidade, pf.dataValidade) " +
            "FROM ProdutoFornecedor pf JOIN pf.produto p " +
            "WHERE pf.quantidade <= :minimo AND pf.quantidade > 0 AND pf.produtoAtivo = true")
    List<AlertaEstoqueDTO> buscarEstoqueBaixo(@Param("minimo") BigDecimal minimo);

}



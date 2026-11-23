package com.api.repository;

import com.api.dto.AlertaEstoqueDTO;
import com.api.dto.BalancaProdutoDTO;
import com.api.dto.EstoqueItemDTO;
import com.api.dto.ProdutoFornecedorDTO;
import com.api.entity.Fornecedor;
import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedor, Long> {

    @Query("SELECT pf FROM ProdutoFornecedor pf WHERE produto.id = :produtoId")
    ProdutoFornecedor getProdutoFornecedor(@Param("produtoId") Long produtoId);

    List<ProdutoFornecedor> findByProdutoIdIn(List<Long> produtoIds);

    Optional<ProdutoFornecedor> findByProdutoAndFornecedor(Produto produto, Fornecedor fornecedor);

    @Query("""
            SELECT pf FROM ProdutoFornecedor pf 
            WHERE pf.produto.id = :produtoId
            AND pf.produtoAtivo = true
            ORDER BY pf.precoVenda ASC
            """)
    List<ProdutoFornecedor> getProdutoFornecedorList(Long produtoId, Pageable pageable);

    @Query("SELECT pf FROM ProdutoFornecedor pf JOIN FETCH pf.produto " +
            "WHERE pf.produto.id = :produtoId AND pf.quantidade > :qtd " +
            "ORDER BY pf.dataValidade ASC")
    List<ProdutoFornecedor> buscarLotesDisponiveis(
            @Param("produtoId") Long produtoId,
            @Param("qtd") BigDecimal qtd
    );

    @Query("""
        SELECT pf FROM ProdutoFornecedor pf 
        WHERE pf.produto.id = :produtoId 
          AND pf.fornecedor.id = :fornecedorId 
        ORDER BY pf.dataValidade ASC
        """)
    Optional<ProdutoFornecedor> getProdutoFornecedor(
            @Param("produtoId") Long produtoId,
            @Param("fornecedorId") Long fornecedorId);


    @Query("SELECT pf FROM ProdutoFornecedor pf JOIN FETCH pf.produto p " +
            "WHERE p.id = :produtoId AND pf.produtoAtivo = true AND pf.quantidade >= 0 " +
            "ORDER BY pf.dataValidade ASC")
    List<ProdutoFornecedor> findAtivosPorProduto(@Param("produtoId") Long produtoId);


    //PARA DASHBOARD
    @Query("SELECT new com.api.dto.AlertaEstoqueDTO(p.nome, pf.quantidade, pf.dataValidade) " +
            "FROM ProdutoFornecedor pf JOIN pf.produto p " +
            "WHERE pf.dataValidade <= :dataLimite AND pf.quantidade > 0 AND pf.produtoAtivo = true")
    List<AlertaEstoqueDTO> buscarProdutosVencendo(@Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT new com.api.dto.AlertaEstoqueDTO(p.nome, pf.quantidade, pf.dataValidade) " +
            "FROM ProdutoFornecedor pf JOIN pf.produto p " +
            "WHERE pf.quantidade <= pf.estoqueMinimo AND pf.quantidade > 0 AND pf.produtoAtivo = true")
    List<AlertaEstoqueDTO> buscarEstoqueBaixo();

    @Query("SELECT NEW com.api.dto.AlertaEstoqueDTO(p.nome, pf.quantidade, pf.dataValidade) " +
            "FROM ProdutoFornecedor pf JOIN pf.produto p " +
            "WHERE pf.quantidade = 0 AND pf.produtoAtivo = true AND p.ativo = true")
    List<AlertaEstoqueDTO> buscarSemEstoque();

    @Query("SELECT pf FROM ProdutoFornecedor pf JOIN FETCH pf.produto p WHERE pf.quantidade <= pf.estoqueMinimo")
    List<ProdutoFornecedor> findEstoqueCritico();


    //TRAZER DADOS PARA ESTOQUE DE FORMA RAPIDA E DIRETA
    @Query("""
    SELECT NEW com.api.dto.EstoqueItemDTO(
        pf.id,
        p.id,
        COALESCE(p.codigoProduto, p.codigoBarras),
        p.nome,
        f.nome,
        pf.quantidade,
        pf.estoqueMinimo,
        pf.unidadeMedida,
        pf.precoCompra,
        pf.precoVenda,
        pf.dataValidade,
        pf.emPromocao,
        pf.valorPromocional
    )
    FROM ProdutoFornecedor pf
    JOIN pf.produto p
    LEFT JOIN pf.fornecedor f
    WHERE pf.produtoAtivo = true AND p.ativo = true
""")
    List<EstoqueItemDTO> listarEstoqueCompletoOTimizado();


    @Query("""
            SELECT new com.api.dto.ProdutoFornecedorDTO(
                p.codigoProduto,
                p.nome,
                pf.produto.id,
                pf.quantidade,
                pf.dataValidade,
                pf.precoVenda,
                pf.estoqueMinimo,
                pf.unidadeMedida
            )
            FROM ProdutoFornecedor pf
            INNER JOIN pf.produto p
            WHERE p.codigoProduto = :codigoProduto
              AND pf.quantidade >= 0 AND pf.produtoAtivo = true
            ORDER BY pf.dataValidade ASC
        """)
    List<ProdutoFornecedorDTO> buscarGranelPorCodigoLoja(
            @Param("codigoProduto") String codigoProduto
    );

    @Modifying
    @Query("UPDATE ProdutoFornecedor pf SET pf.precoVenda = :novoPreco WHERE pf.produto.id = :produtoId AND pf.produtoAtivo = true")
    int atualizarPrecoVendaPorProduto(
            @Param("produtoId") Long produtoId,
            @Param("novoPreco") BigDecimal novoPreco
    );


    @Query("SELECT pf FROM ProdutoFornecedor pf WHERE pf.quantidade >= :qtd AND pf.produtoAtivo = true")
    List<ProdutoFornecedor> buscarComEstoqueEAtivosApp(@Param("qtd") Integer qtd);

    //INATIVA E ATIVAR PELO PRODUTO.
    @Modifying
    @Transactional
    @Query("UPDATE ProdutoFornecedor pf " +
            "SET pf.produtoAtivo = :ativo, " +
            "    pf.quantidade = CASE WHEN :ativo = false THEN 0 ELSE pf.quantidade END " +
            "WHERE pf.produto.id = :produtoId")
    int atualizarProdutoAtivoPorProdutoId(@Param("produtoId") Long produtoId, @Param("ativo") boolean ativo);

}



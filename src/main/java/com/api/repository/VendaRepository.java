package com.api.repository;

import com.api.eNum.StatusPedido;
import com.api.entity.Venda;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository  extends JpaRepository<Venda, Long> {

    // Consulta simples por período (sem join fetch de múltiplas listas)
    @Query("SELECT v FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim ORDER BY v.dataVenda DESC")
    List<Venda> findByDataVendaBetweenOrderByDataVendaDesc(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT v FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim ORDER BY v.dataVenda DESC")
    List<Venda> findByDataVendaBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT DISTINCT v FROM Venda v " +
            "LEFT JOIN v.cliente c " +
            "WHERE (:termo IS NULL OR :termo = '' OR CAST(v.id AS string) LIKE %:termo% OR (c IS NOT NULL AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')))) " +
            "ORDER BY v.id DESC")
    List<Venda> buscarVendasGerais(@Param("termo") String termo);

    @Query("SELECT DISTINCT v FROM Venda v " +
            "LEFT JOIN v.cliente c " +
            "WHERE v.dataVenda >= :inicio " +
            "AND v.dataVenda < :fim " +
            "AND (:termo IS NULL OR :termo = '' " +
            "OR CAST(v.id AS string) LIKE CONCAT('%', :termo, '%') " +
            "OR (c IS NOT NULL AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')))) " +
            "ORDER BY v.id DESC")
    List<Venda> buscarVendaDia(
            @Param("termo") String termo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
    SELECT COALESCE(SUM(v.total), 0)
    FROM Venda v
    WHERE v.statusPedido = com.api.eNum.StatusPedido.FINALIZADO
      AND v.dataVenda >= :inicio
      AND v.dataVenda < :fim
    """)
    BigDecimal somarVendasFinalizadasPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("SELECT DISTINCT v FROM Venda v " +
            "LEFT JOIN v.cliente c " +
            "WHERE (:termo IS NULL OR :termo = '' " +
            "OR CAST(v.id AS string) LIKE CONCAT('%', :termo, '%') " +
            "OR (c IS NOT NULL AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')))) " +
            "ORDER BY v.id DESC")
    List<Venda> pesquisarVendasPorIdOuCliente(
            @Param("termo") String termo
    );


    /*
    * CONSULTA PARA RELATORIOS
    * */
    @Query("""
    SELECT COUNT(v)
    FROM Venda v
    WHERE v.dataVenda >= :inicio
      AND v.dataVenda < :fim
    """)
    long contarVendasPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
    SELECT COUNT(v)
    FROM Venda v
    WHERE v.statusPedido = :status
      AND v.dataVenda >= :inicio
      AND v.dataVenda < :fim
    """)
    long contarPorStatusEPeriodo(
            @Param("status") StatusPedido status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
    SELECT COALESCE(SUM(v.total), 0)
    FROM Venda v
    WHERE v.statusPedido = :status
      AND v.dataVenda >= :inicio
      AND v.dataVenda < :fim
    """)
    BigDecimal somarPorStatusEPeriodo(
            @Param("status") StatusPedido status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
    SELECT v
    FROM Venda v
    WHERE v.dataVenda >= :inicio
      AND v.dataVenda < :fim
    ORDER BY v.dataVenda DESC
    """)
    List<Venda> buscarVendasRecentes(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable
    );

    @Query("""
    SELECT FUNCTION('DATE', v.dataVenda),
           COALESCE(SUM(v.total), 0),
           COUNT(v.id)
    FROM Venda v
    WHERE v.statusPedido = :status
      AND v.dataVenda >= :inicio
      AND v.dataVenda < :fim
    GROUP BY FUNCTION('DATE', v.dataVenda)
    ORDER BY FUNCTION('DATE', v.dataVenda)
    """)
    List<Object[]> agruparFaturamentoDiario(
            @Param("status") StatusPedido status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
    SELECT p.tipo,
           COALESCE(SUM(p.valor), 0),
           COUNT(p.id)
    FROM PagamentoVenda p
    JOIN p.venda v
    WHERE v.statusPedido = :status
      AND v.dataVenda >= :inicio
      AND v.dataVenda < :fim
    GROUP BY p.tipo
    """)
    List<Object[]> agruparPagamentos(
            @Param("status") StatusPedido status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
    SELECT i.produto.id,
           i.produto.nome,
           COALESCE(SUM(i.quantidade), 0),
           COALESCE(SUM(i.subtotal), 0)
    FROM ItemVenda i
    JOIN i.venda v
    WHERE v.statusPedido = :status
      AND v.dataVenda >= :inicio
      AND v.dataVenda < :fim
      AND i.produto IS NOT NULL
    GROUP BY i.produto.id, i.produto.nome
    ORDER BY SUM(i.quantidade) DESC
    """)
    List<Object[]> agruparProdutosVendidos(
            @Param("status") StatusPedido status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );



}

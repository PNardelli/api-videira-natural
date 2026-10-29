package com.api.repository;

import com.api.entity.Venda;
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
    List<Venda> buscarVendasDoisDias(
            @Param("termo") String termo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("SELECT SUM(v.total) FROM Venda v WHERE v.statusPedido = 'FINALIZADO' AND v.dataVenda >= :inicioPeriodo")
    BigDecimal somarVendasAPartirDe(@Param("inicioPeriodo") LocalDateTime inicioPeriodo);

    @Query("SELECT DISTINCT v FROM Venda v " +
            "LEFT JOIN v.cliente c " +
            "WHERE (:termo IS NULL OR :termo = '' " +
            "OR CAST(v.id AS string) LIKE CONCAT('%', :termo, '%') " +
            "OR (c IS NOT NULL AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')))) " +
            "ORDER BY v.id DESC")
    List<Venda> pesquisarVendasPorIdOuCliente(
            @Param("termo") String termo
    );
}

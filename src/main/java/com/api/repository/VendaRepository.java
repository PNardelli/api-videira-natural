package com.api.repository;

import com.api.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository  extends JpaRepository<Venda, Long> {

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venda v " +
            "WHERE v.dataVenda >= :inicio AND v.dataVenda <= :fim")
    BigDecimal calcularFaturamento(@Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim);

    // Se quiser o total de vendas por tipo (Online/PDV) diretamente:
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venda v " +
            "WHERE v.dataVenda >= :inicio AND v.dataVenda <= :fim " +
            "AND (:tipo IS NULL OR v.canalVenda = :tipo)")
    BigDecimal calcularFaturamentoPorTipo(@Param("inicio") LocalDateTime inicio,
                                          @Param("fim") LocalDateTime fim,
                                          @Param("tipo") String tipo);

    // Consulta simples por período (sem join fetch de múltiplas listas)
    @Query("SELECT v FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim ORDER BY v.dataVenda DESC")
    List<Venda> findByDataVendaBetweenOrderByDataVendaDesc(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Consulta com termo e período
    @Query("SELECT v FROM Venda v LEFT JOIN v.cliente c WHERE (str(v.id) LIKE %:termo% OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))) AND v.dataVenda BETWEEN :inicio AND :fim ORDER BY v.dataVenda DESC")
    List<Venda> findByTermoOrDataVendaBetween(@Param("termo") String termo, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}

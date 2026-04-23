package com.api.repository;

import com.api.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

}

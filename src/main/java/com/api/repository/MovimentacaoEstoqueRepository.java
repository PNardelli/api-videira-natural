package com.api.repository;

import com.api.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

        @Query("SELECT SUM(CASE WHEN m.tipoMovimentacao = 'ENTRADA' THEN m.quantidade ELSE -m.quantidade END) " +
                "FROM MovimentacaoEstoque m WHERE m.produto.id = :produtoId")
        BigDecimal getSaldoAtual(@Param("produtoId") Long produtoId);
    }


package com.api.repository;

import com.api.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    // Lista todas ordenadas da mais recente para a mais antiga
    List<MovimentacaoEstoque> findAllByOrderByDataDesc();

    // Novo método para buscar movimentações dentro de um mês/ano específico (passando o inicio e o fim do mês)
    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.data >= :inicio AND m.data <= :fim ORDER BY m.data DESC")
    List<MovimentacaoEstoque> buscarPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}

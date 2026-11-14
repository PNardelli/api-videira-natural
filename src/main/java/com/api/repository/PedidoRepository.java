package com.api.repository;

import com.api.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"itens"})
    @Query("SELECT p FROM Pedido p WHERE p.dataHora >= :dataLimite ORDER BY p.dataHora DESC")
    List<Pedido> findRecentesComItens(@Param("dataLimite") LocalDateTime dataLimite);

}

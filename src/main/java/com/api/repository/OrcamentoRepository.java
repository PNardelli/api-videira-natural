package com.api.repository;

import com.api.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    Optional<Orcamento> findByCodigoRecuperacao(String codigo);

    @Modifying
    @Transactional
    @Query("DELETE FROM Orcamento o WHERE o.createdAt < :limite")
    void deleteExpiredOrcamentos(LocalDateTime limite);
}


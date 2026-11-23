package com.api.repository;

import com.api.entity.FaturamentoMensal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaturamentoMensalRepository extends JpaRepository<FaturamentoMensal, Long> {

    List<FaturamentoMensal> findByAnoOrderByMesAsc(Integer ano);
    Optional<FaturamentoMensal> findByAnoAndMes(Integer ano, Integer mes);
}
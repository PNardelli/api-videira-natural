package com.api.repository;

import com.api.entity.MensalidadeSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MensalidadeSistemaRepository extends JpaRepository<MensalidadeSistema, Long> {
    List<MensalidadeSistema> findByAnoOrderByMesAsc(Integer ano);
    Optional<MensalidadeSistema> findByAnoAndMes(Integer ano, Integer mes);
}
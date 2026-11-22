package com.api.repository;

import com.api.entity.HorarioLoja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioLojaRepository extends JpaRepository<HorarioLoja, Long> {
}

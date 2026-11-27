package com.api.repository;

import com.api.entity.FaixaFrete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaixaFreteRepository extends JpaRepository<FaixaFrete, Long> {

}

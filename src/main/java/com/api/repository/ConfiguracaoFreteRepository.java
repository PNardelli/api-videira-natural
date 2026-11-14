package com.api.repository;

import com.api.entity.ConfiguracaoFrete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracaoFreteRepository extends JpaRepository<ConfiguracaoFrete, Long> {

}

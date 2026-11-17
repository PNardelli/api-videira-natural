package com.api.repository;

import com.api.entity.Promocao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

    List<Promocao> findByOcultarFalse();

    Optional<Promocao> findByCodigoPromocaoAndAtivo(String codigoPromocao, boolean ativo);

}

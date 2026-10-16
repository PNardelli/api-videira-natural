package com.api.repository;

import com.api.entity.ValeTroca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValeTrocaRepository extends JpaRepository<ValeTroca, Long> {

    // Método customizado para o Spring Data JPA gerar a query automaticamente
    // baseada no atributo 'codigoBarra' da entidade ValeTroca
    Optional<ValeTroca> findByCodigoBarra(String codigoBarra);

}

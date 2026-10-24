package com.api.repository;

import com.api.dto.CompraResumoDTO;
import com.api.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    @Query("SELECT new com.api.dto.CompraResumoDTO(c.id, c.dataHora, f.nome, COUNT(i), c.status, c.previsaoEntrega, c.dataEntrega) " +
            "FROM Compra c " +
            "LEFT JOIN c.fornecedor f " +
            "LEFT JOIN c.itens i " +
            "GROUP BY c.id, c.dataHora, f.nome, c.status, c.previsaoEntrega, c.dataEntrega")
    List<CompraResumoDTO> listarResumoCompras();

}
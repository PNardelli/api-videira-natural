package com.api.repository;

import com.api.dto.CompraDTO;
import com.api.dto.CompraResumoDTO;
import com.api.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    @Query("SELECT new com.api.dto.CompraResumoDTO(c.id, c.dataHora, f.nome, COUNT(i), c.status, c.previsaoEntrega, c.dataEntrega, c.identificadorNota) " +
            "FROM Compra c " +
            "LEFT JOIN c.fornecedor f " +
            "LEFT JOIN c.itens i " +
            "GROUP BY c.id, c.dataHora, f.nome, c.status, c.previsaoEntrega, c.dataEntrega " +
            "ORDER BY c.id DESC")
    List<CompraResumoDTO> listarResumoCompras();

    @Modifying
    @Query(value = """
    DELETE FROM tb_itens_compra
    WHERE id = :itemId
      AND compra_id = :compraId
    """, nativeQuery = true)
    int excluirItemDaCompra(
            @Param("itemId") Long itemId,
            @Param("compraId") Long compraId
    );

    @Modifying
    @Query(value = """
    SELECT * FROM tb_itens_compra
    WHERE id = :itemId
      AND compra_id = :compraId
    """, nativeQuery = true)
    CompraDTO editarItemDaCompra(
            @Param("itemId") Long itemId,
            @Param("compraId") Long compraId
    );

    @Modifying
    @Query(value = """
    UPDATE itens_compra
       SET valor_unitario = :valor
     WHERE id = :idItem
       AND compra_id = :idCompra
    """, nativeQuery = true)
    int atualizarValorItem(
            @Param("idItem") Long idItem,
            @Param("idCompra") Long idCompra,
            @Param("valor") BigDecimal valor
    );

}
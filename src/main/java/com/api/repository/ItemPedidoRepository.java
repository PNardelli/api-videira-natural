package com.api.repository;

import com.api.eNum.StatusItemPedido;
import com.api.entity.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    List<ItemPedido> findByPedidoIdAndStatusReserva(
            Long pedidoId,
            StatusItemPedido statusReserva
    );

}

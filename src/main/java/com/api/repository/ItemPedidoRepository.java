package com.api.repository;

import com.api.entity.ItemPedido;
import com.api.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    List<ItemPedido> findByPedido(Pedido pedido);
}


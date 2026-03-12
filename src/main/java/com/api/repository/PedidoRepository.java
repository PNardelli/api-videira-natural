package com.api.repository;

import com.api.entity.Pedido;
import com.api.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

}

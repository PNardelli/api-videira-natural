package com.api.repository;

import com.api.entity.PushSubscriptionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionClienteRepository extends JpaRepository<PushSubscriptionCliente, Long> {

    List<PushSubscriptionCliente> findByClienteId(Long clienteId);

    Optional<PushSubscriptionCliente> findByEndpoint(String endpoint);

}

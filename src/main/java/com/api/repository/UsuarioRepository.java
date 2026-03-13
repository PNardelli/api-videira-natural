package com.api.repository;

import com.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // O Spring Data JPA implementa a lógica desta busca sozinho
    UserDetails findByLogin(String login);
}

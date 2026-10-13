package com.api.repository;

import com.api.dto.ClienteDTO;
import com.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Cliente save(Cliente cliente);

    boolean existsByWhatsapp(String whatsapp);

    // Verifica se existe o WhatsApp OU o Email (para novos cadastros)
    boolean existsByWhatsappOrEmail(String whatsapp, String email);

    // Para EDIÇÕES: Verifica se existe em outro ID
    boolean existsByWhatsappOrEmailAndIdNot(String whatsapp, String email, Long id);

    @Query("""
    SELECT c FROM Cliente c
    WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
       OR c.whatsapp LIKE CONCAT('%', :termo, '%')
""")
    List<Cliente> buscarPorNomeOuWhatsapp(@Param("termo") String termo);

}

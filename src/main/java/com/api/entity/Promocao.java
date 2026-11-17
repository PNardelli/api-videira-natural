package com.api.entity;

import com.api.dto.ItemKitDTO;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import javax.annotation.security.DenyAll;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tb_promocoes")
@Data
public class Promocao {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true, nullable = false, length = 14)
        private String codigoPromocao; // Ex: PROMO123456123

        private String nome; // Ex: Combo Whey + Creatina
        private BigDecimal precoPromocional;
        private boolean ativo = true;
        private LocalDateTime dataCriacao = LocalDateTime.now();

        private boolean ocultar = false;

        private long quantidadeCombos;

        // Guarda todos os itens do kit diretamente aqui numa coluna JSON
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        private List<ItemKitDTO> itens;
}

package com.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tb_horario_loja")
public class HorarioLoja {

        @Id
        private Long id = 1L;

        // Sobrescreve todos os dias quando true (Feriado, Problemas Pessoais, etc.)
        @Column(nullable = false)
        private Boolean fechadoManual = false;

        // JSON array com Segunda a Domingo: [{"dia":"SEGUNDA","label":"Segunda-feira","habilitado":true,"abre":"08:00","fecha":"18:00"}, ...]
        @Column(columnDefinition = "TEXT")
        private String horariosJson;
}

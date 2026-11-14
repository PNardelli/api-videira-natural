package com.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "tb_frete")
@Entity
@Data
public class ConfiguracaoFrete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean ativo;

    private String enderecoOrigem;
    private String bairroOrigem;
    private String cepOrigem;
    private String cidadeOrigem;
    private String estadoOrigem;

    // Mudamos para OneToMany com Cascade e orphanRemoval
    @OneToMany(mappedBy = "configuracaoFrete", fetch = FetchType.EAGER ,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaixaFrete> faixas = new ArrayList<>();

    private LocalDateTime dataCriacao;

}

package com.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tb_importacoesXml")
public class ImportacoesXML {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private LocalDate dataImportacao;

    @Column(unique = true, name = "idUnicoNotaFiscal", nullable = false)
    private String idUnicoNotaFiscal;

    private String nomeFornecedor;




}

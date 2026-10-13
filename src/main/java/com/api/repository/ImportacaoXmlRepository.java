package com.api.repository;

import com.api.entity.ImportacoesXML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportacaoXmlRepository extends JpaRepository<ImportacoesXML, Long> {

    boolean existsByIdUnicoNotaFiscal(String idUnicoNotaFiscal);

    void deleteByIdUnicoNotaFiscal(String idUnicoNotaFiscal);
}

package com.api.dto.xml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportacaoResultadoDTO {
    private boolean notaJaImportada;
    private List<ItemConferenciaDTO> itens;
}

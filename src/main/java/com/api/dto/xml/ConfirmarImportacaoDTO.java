package com.api.dto.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConfirmarImportacaoDTO {

    private List<ItemConferenciaDTO> itens;

    private Boolean reimportar;

}

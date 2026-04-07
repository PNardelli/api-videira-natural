package com.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConfirmarImportacaoRequest {
    private List<ProdutoComparativoDto> existentes;
    private List<xmlDTO> novos;

    public List<ProdutoComparativoDto> getExistentes() { return existentes; }
    public void setExistentes(List<ProdutoComparativoDto> existentes) { this.existentes = existentes; }
    public List<xmlDTO> getNovos() { return novos; }
    public void setNovos(List<xmlDTO> novos) { this.novos = novos; }
}

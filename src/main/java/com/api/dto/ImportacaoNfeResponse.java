package com.api.dto;

import com.api.entity.Produto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ImportacaoNfeResponse {
    private List<ProdutoComparativoDto> existentes = new ArrayList<>();
    private List<xmlDTO> novos = new ArrayList<>();

    // Getters e Setters
    public List<ProdutoComparativoDto> getExistentes() { return existentes; }
    public void setExistentes(List<ProdutoComparativoDto> existentes) { this.existentes = existentes; }
    public List<xmlDTO> getNovos() { return novos; }
    public void setNovos(List<xmlDTO> novos) { this.novos = novos; }

    public void addProdutoExistente(Produto p, BigDecimal qtdNota) {
        this.existentes.add(new ProdutoComparativoDto(p, qtdNota));
    }

    public void addProdutoNovo(xmlDTO item) {
        this.novos.add(item);
    }
}

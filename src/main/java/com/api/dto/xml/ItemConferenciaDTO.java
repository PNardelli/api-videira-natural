package com.api.dto.xml;

import com.api.eNum.UnidadeMedida;
import com.api.entity.Fornecedor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ItemConferenciaDTO {
    private String idUnicoNotaFiscal;
    private String codigoFornecedor;
    private String codigoProdutoInterno;
    private String nomeOriginalXml; // "CHIPS DE COCO A GRANEL kg"
    private String nomeSugerido;     // "CHIPS DE COCO" (Você pode tratar com String.replace)
    private BigDecimal quantidade;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;  // Novo
    private LocalDate dataValidade; // Novo
    private String unidadeMedida;
    private boolean existeNoSistema; // Para o Vue marcar em verde ou vermelho
    private Long produtoId;          // Preenchido se existeNoSistema for true
    private Long categoriaId;
    private Long fornecedorId;
}

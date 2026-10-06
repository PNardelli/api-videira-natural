package com.api.controller;

import com.api.dto.BalancaProdutoDTO;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/balanca")
public class BalancaController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoFornecedorRepository pfRepository;

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarCargaBalanca() {
        List<BalancaProdutoDTO> produtosBalanca = produtoRepository.buscarDadosUnicosParaBalanca();
        StringBuilder arquivoBuilder = new StringBuilder();

        for (BalancaProdutoDTO p : produtosBalanca) {
            String codProd = p.getCodigoProduto();

            if (codProd == null || codProd.trim().isEmpty() || codProd.trim().length() > 6) {
                continue;
            }

            long codigoNumerico;
            try {
                codigoNumerico = Long.parseLong(codProd.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            String strPlu = String.format("%07d", codigoNumerico);

            long precoCentavos = 0L;
            if (p.getPreco() != null) {
                precoCentavos = p.getPreco().multiply(new java.math.BigDecimal("10000")).longValue();
            }
            String strPreco = String.format("%04d", precoCentavos);

            int diasValidadeInt = 0;
            if (p.getDataValidade() != null) {
                try {
                    diasValidadeInt = Integer.parseInt(String.valueOf(p.getDataValidade()));
                } catch (NumberFormatException ignored) {}
            }
            String validade = String.format("%03d", diasValidadeInt);

            String nomeBruto = p.getNome() != null ? p.getNome() : "PRODUTO";
            String nomeFormatado = String.format("%-30.30s", nomeBruto.toUpperCase());

            // Linha formatada com codificação e padrão MGV
            String linha = "02" + strPlu + strPreco + validade + nomeFormatado
                    + "0000000000000000110000000000000000000000000000000000000000000000000000000000000000\r\n";

            arquivoBuilder.append(linha);
        }

        try {
            // Converte para bytes utilizando ISO-8859-1 para preservar acentos (ex: Grão, Moída)
            byte[] bytesArquivo = arquivoBuilder.toString().getBytes(StandardCharsets.ISO_8859_1);

            // Retorna o ficheiro diretamente para download no browser
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ITENSMGV.TXT")
                    .contentType(MediaType.parseMediaType("text/plain; charset=ISO-8859-1"))
                    .body(bytesArquivo);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
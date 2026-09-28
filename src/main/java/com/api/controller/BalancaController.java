package com.api.controller;

import com.api.entity.Produto;
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

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarCargaBalanca() {
        // Busca todos os produtos registados
        List<Produto> produtos = produtoRepository.findAll();
        StringBuilder arquivoBuilder = new StringBuilder();

        for (Produto p : produtos) {
            // 1. PLU / Código da Balança (6 dígitos, com zeros à esquerda)
            // Se não tiver um campo 'plu' específico, podes usar o ID do produto
            // 1. PLU / Código da Balança (converte a String para Long e formata com 6 dígitos)
            Long codigoNumerico = (p.getCodigoProduto() != null && !p.getCodigoProduto().isEmpty())
                    ? Long.parseLong(p.getCodigoProduto())
                    : p.getId();
            String strPlu = String.format("%06d", codigoNumerico);

            // 2. Tipo de Preço (1 para peso/unidade padrão)
            String tipoPreco = "1";

// 3. Preço com BigDecimal multiplicado por 10000 (8 dígitos, com zeros à esquerda)
            long precoCentavos = 0L;
            if (p.getPrecoVenda() != null) {
                // Multiplica o BigDecimal por 10000 e converte para long de forma segura
                precoCentavos = p.getPrecoVenda().multiply(new java.math.BigDecimal("10000")).longValue();
            }
            String strPreco = String.format("%08d", precoCentavos);

            // 4. Validade padrão em dias (3 dígitos, ex: 003)
            String validade = "003";

            // 5. Nome do Produto (Exatamente 30 caracteres, maiúsculas, alinhado à esquerda)
            String nomeBruto = p.getNome() != null ? p.getNome() : "PRODUTO";
            String nomeFormatado = String.format("%-30.30s", nomeBruto.toUpperCase());

            // 6. Montagem da linha completa com o sufixo padrão do MGV Toledo
            String linha = "01" + strPlu + tipoPreco + strPreco + validade + nomeFormatado
                    + "0000000000000000110000000000000000000000000000000000000000000000000000000000000000\r\n";

            arquivoBuilder.append(linha);
        }

        // Converte o texto para bytes usando UTF-8 ou ISO-8859-1 (padrão de sistemas legados)
        byte[] bytesArquivo = arquivoBuilder.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Configura os headers para forçar o download do ficheiro no navegador
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "ITENSMGV.TXT");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytesArquivo);
    }
}
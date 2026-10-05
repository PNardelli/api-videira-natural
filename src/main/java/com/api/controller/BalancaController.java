package com.api.controller;

import com.api.entity.Produto;
import com.api.entity.ProdutoFornecedor;
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
        List<ProdutoFornecedor> produtos = pfRepository.findAll();
        StringBuilder arquivoBuilder = new StringBuilder();

        for (ProdutoFornecedor p : produtos) {
            String codProd = p.getProduto().getCodigoProduto();

            // REGRA: Se o produto não tiver um código de produto curto (ex: PLU até 6 dígitos)
            // e possuir apenas código de barras longo, ele é ignorado pela balança.
            if (codProd == null || codProd.trim().isEmpty() || codProd.trim().length() > 6) {
                // Pula este item e vai para o próximo (não exporta itens de código de barras)
                continue;
            }

            long codigoNumerico;
            try {
                codigoNumerico = Long.parseLong(codProd.trim());
            } catch (NumberFormatException e) {
                // Se não for puramente numérico, ignora
                continue;
            }
            String strPlu = String.format("%07d", codigoNumerico);

            // 2. Tipo de Preço (1 para peso/unidade padrão)
            //String tipoPreco = "1";

            // 3. Preço multiplicado por 10000 (8 dígitos, com zeros à esquerda)
            long precoCentavos = 0L;
            if (p.getPrecoVenda() != null) {
                precoCentavos = p.getPrecoVenda().multiply(new java.math.BigDecimal("10000")).longValue();
            }
            String strPreco = String.format("%06d", precoCentavos);

            // 4. Validade em dias (3 dígitos, ex: 000 ou 365, com segurança contra nulos)
            int diasValidadeInt = 0;
            if (p.getDataValidadeDias() != null) {
                try {
                    diasValidadeInt = Integer.parseInt(p.getDataValidadeDias().trim());
                } catch (NumberFormatException ignored) {}
            }
            String validade = String.format("%03d", diasValidadeInt);

            // 5. Nome do Produto (Exatamente 30 caracteres, maiúsculas, alinhado à esquerda)
            String nomeBruto = p.getProduto().getNome() != null ? p.getProduto().getNome() : "PRODUTO";
            String nomeFormatado = String.format("%-30.30s", nomeBruto.toUpperCase());

            // 6. Montagem da linha completa com o sufixo padrão do MGV Toledo
            String linha = "02" + strPlu + strPreco + validade + nomeFormatado
                    + "0000000000000000110000000000000000000000000000000000000000000000000000000000000000\r\n";

            System.out.println(linha);

            arquivoBuilder.append(linha);
        }

        byte[] bytesArquivo = arquivoBuilder.toString().getBytes(StandardCharsets.ISO_8859_1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "ITENSMGV.TXT");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytesArquivo);
    }
}
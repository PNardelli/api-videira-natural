package com.api.controller;

import com.api.dto.BalancaProdutoDTO;
import com.api.repository.ProdutoFornecedorRepository;
import com.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/balanca")
public class BalancaController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoFornecedorRepository pfRepository;

    @GetMapping("/exportar")
    public ResponseEntity<Map<String, Object>> exportarCargaBalanca() {
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

            // Linha formatada
            String linha = "02" + strPlu + strPreco + validade + nomeFormatado
                    + "0000000000000000110000000000000000000000000000000000000000000000000000000000000000\r\n";

            arquivoBuilder.append(linha);
        }

        // Caminho fixo na máquina onde o backend está rodando
        String caminhoDiretorio = "C:\\sistemavideira\\balanca\\";
        Path caminhoCompleto = Paths.get(caminhoDiretorio, "ITENSMGV.TXT");

        try {
            // Garante que a pasta existe
            Files.createDirectories(caminhoCompleto.getParent());

            // GRAVAÇÃO FORÇADA EM ISO-8859-1 (Resolve 100% o problema dos acentos como Grão e Moída)
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(caminhoCompleto.toFile()), StandardCharsets.ISO_8859_1))) {
                writer.write(arquivoBuilder.toString());
            }

            // Retorna estritamente um JSON para o front-end exibir apenas o alerta
            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Carga exportada e salva com sucesso na pasta da balança!"
            ));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "sucesso", false,
                    "mensagem", "Erro ao salvar arquivo no diretório: " + e.getMessage()
            ));
        }
    }
}
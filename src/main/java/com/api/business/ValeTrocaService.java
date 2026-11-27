package com.api.business;

import com.api.entity.ValeTroca;
import com.api.repository.ValeTrocaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ValeTrocaService {

    @Autowired
    private ValeTrocaRepository valeTrocaRepository;

    @Transactional
    public ValeTroca gerarValeTroca(Long vendaOrigemId, Long clienteId, BigDecimal valorOriginal, int diasValidade, List<?> itensDevolvidos) {
        if (valorOriginal == null || valorOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do vale-troca deve ser maior que zero.");
        }

        // Instancia o objeto usando o construtor da entidade (que já gera o código de barras e define a validade)
        int validadeDias = diasValidade > 0 ? diasValidade : 90;
        ValeTroca novoVale = new ValeTroca(vendaOrigemId, clienteId, valorOriginal, validadeDias);

        // Opcional: Aqui você pode adicionar a lógica para devolver os itens ao estoque, se desejar.
        // if (itensDevolvidos != null) { ... }

        return valeTrocaRepository.save(novoVale);
    }

    public Optional<ValeTroca> buscarPorCodigo(String codigoBarra) {
        return valeTrocaRepository.findByCodigoBarra(codigoBarra);
    }

    public ValeTroca cadastrarCupom(ValeTroca valeTroca){
        return valeTrocaRepository.save(valeTroca);
    }

    public ResponseEntity<Map<String, Object>> validarCupom(String codigo) {
        String termo = codigo == null ? "" : codigo.trim().toUpperCase();
        if (termo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valido", false,
                    "message", "Informe o código do cupom."
            ));
        }

        var opt = valeTrocaRepository.findByCodigoCupom(termo);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "valido", false,
                    "message", "Cupom não encontrado."
            ));
        }

        ValeTroca cupom = opt.get();

        if (cupom.getStatus() != null && !"ATIVO".equalsIgnoreCase(cupom.getStatus().name())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valido", false,
                    "message", "Cupom indisponível ou já utilizado."
            ));
        }

        if (cupom.getDataValidade() != null && cupom.getDataValidade().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valido", false,
                    "message", "Cupom expirado."
            ));
        }

        String tipo = cupom.getTipoDesconto() != null ? cupom.getTipoDesconto() : "VALOR_FIXO";
        java.math.BigDecimal valorAplicavel = "PERCENTUAL".equalsIgnoreCase(tipo)
                ? cupom.getValor()
                : (cupom.getValorSaldo() != null ? cupom.getValorSaldo() : cupom.getValor());

        if (valorAplicavel == null || valorAplicavel.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valido", false,
                    "message", "Cupom sem saldo ou desconto disponível."
            ));
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("valido", true);
        resposta.put("codigo", cupom.getCodigoCupom().toUpperCase());
        resposta.put("tipoDesconto", tipo);
        resposta.put("valor", valorAplicavel);

        return ResponseEntity.ok(resposta);
    }
}

package com.api.business;

import com.api.entity.ValeTroca;
import com.api.repository.ValeTrocaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
}

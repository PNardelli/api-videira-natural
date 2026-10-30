package com.api.uteis;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenCacheService {

    // Estrutura para guardar: Chave (E-mail ou WhatsApp) -> Dados do Token
    private final ConcurrentHashMap<String, TokenInfo> cacheTokens = new ConcurrentHashMap<>();

    public void salvarToken(String chave, String token) {
        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(10); // Validade de 10 minutos
        cacheTokens.put(chave, new TokenInfo(token, expiracao));
    }

    public boolean validarToken(String chave, String tokenDigitado) {
        TokenInfo info = cacheTokens.get(chave);

        if (info == null) {
            return false; // Token não encontrado ou expirou
        }

        if (LocalDateTime.now().isAfter(info.expiracao)) {
            cacheTokens.remove(chave);
            return false; // Expirado
        }

        boolean valido = info.token.equals(tokenDigitado);
        if (valido) {
            cacheTokens.remove(chave); // Remove após usar com sucesso (uso único)
        }
        return valido;
    }

    private static class TokenInfo {
        String token;
        LocalDateTime expiracao;

        public TokenInfo(String token, LocalDateTime expiracao) {
            this.token = token;
            this.expiracao = expiracao;
        }
    }
}

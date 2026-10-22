package com.api.apiExterno.infinitePay;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfinitePayService {

    private final String INFINITEPAY_URL = "https://api.checkout.infinitepay.io/links";

    public Map<String, Object> criarLinkCheckout(Double valorTotal, String descricao) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        int precoEmCentavos = (int) Math.round(valorTotal * 100);

        Map<String, Object> item = new HashMap<>();
        item.put("quantity", 1);
        item.put("price", precoEmCentavos);
        item.put("description", descricao != null ? descricao : "VIDEIRA NATURAL - PDV");

        Map<String, Object> body = new HashMap<>();
        body.put("handle", "pedro_nardelli");
        body.put("items", List.of(item));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(INFINITEPAY_URL, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("url")) {
                String urlGerada = responseBody.get("url").toString();

                // Extrai o valor do parâmetro lenc da URL gerada
                String hashLenc = "";
                if (urlGerada.contains("lenc=")) {
                    hashLenc = urlGerada.substring(urlGerada.indexOf("lenc=") + 5);
                }

                // Adiciona a hash separada no map de resposta para o front usar se precisar
                responseBody.put("hashLenc", hashLenc);
            }

            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com a API da InfinitePay: " + e.getMessage());
        }
    }
}

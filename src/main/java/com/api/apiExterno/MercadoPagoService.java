package com.api.apiExterno;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MercadoPagoService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String accessToken = "APP_USR-5785880777708319-100810-b063e8c14310df0b6562abaade0a4a37-1327545801"; // APP_USR-...

    public ResponseEntity<Map> criarOrdemPagamento(BigDecimal valor, String tipoPagamento, String externalReference) {
        String url = "https://api.mercadopago.com/v1/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

        // Montagem do corpo da requisição exatamente como testou com sucesso
        Map<String, Object> body = new HashMap<>();
        body.put("type", "point");
        body.put("external_reference", externalReference);
        body.put("description", "Videira Natural - Point");

        // Transactions
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("amount", valor.toString());

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", List.of(paymentMap));
        body.put("transactions", transactions);

        // Config
        Map<String, Object> pointConfig = new HashMap<>();
        pointConfig.put("terminal_id", "NEWLAND_N950__N950NCC804149944");

        Map<String, Object> paymentMethodConfig = new HashMap<>();
        paymentMethodConfig.put("default_type", tipoPagamento); // "debit_card" ou "credit_card"

        Map<String, Object> config = new HashMap<>();
        config.put("point", pointConfig);
        config.put("payment_method", paymentMethodConfig);
        body.put("config", config);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForEntity(url, entity, Map.class);
        } catch (HttpClientErrorException e) {
            // Tratar erro da API do MP
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getResponseBodyAsString()));
        }
    }

    public Map<String, Object> consultarStatusPorReferencia(String externalReference) {
        String url = "https://api.mercadopago.com/v1/orders/search?external_reference=" + externalReference;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // Utilize a mesma variável do seu token de acesso

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> elements = (List<Map<String, Object>>) body.get("elements");

                // Verifica se encontrou alguma ordem com essa referência
                if (elements != null && !elements.isEmpty()) {
                    Map<String, Object> ordem = elements.get(0);

                    // O Mercado Pago retorna o status da ordem (ex: "closed" quando concluída/paga)
                    // Vamos extrair o status e mapear para o front-end
                    String statusOrdem = (String) ordem.get("status"); // Ex: "processed"
                    String statusDetail = (String) ordem.get("status_detail"); // Ex: "accredited"

                    System.out.println("STATUS DA ORDEM: " + statusOrdem + " | DETALHE: " + statusDetail);

                    // Consideramos aprovado se a ordem foi processada e o pagamento foi creditado/aprovado
                    boolean aprovado = "processed".equalsIgnoreCase(statusOrdem) &&
                            "accredited".equalsIgnoreCase(statusDetail);

                    String statusMapeado = aprovado ? "approved" : "pending";

                    return Map.of(
                            "status", statusMapeado,
                            "raw_status", statusOrdem,
                            "order_id", ordem.get("id")
                    );
            }
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Erro ao consultar status da ordem: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Erro inesperado ao consultar status: " + e.getMessage());
        }

        // Retorna pendente caso ainda não tenha retornado sucesso ou não encontre
        return Map.of("status", "pending");
    }
}

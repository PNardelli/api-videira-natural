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
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> elements = (List<Map<String, Object>>) body.get("elements");

                if (elements != null && !elements.isEmpty()) {
                    Map<String, Object> ordem = elements.get(0);

                    // Tenta pegar o status direto da ordem ou da transação de pagamento
                    String statusOrdem = (String) ordem.get("status");
                    String statusDetail = (String) ordem.get("status_detail");

                    // Se não estiver na raiz, procura dentro de transactions.payments
                    if (ordem.containsKey("transactions")) {
                        Map<String, Object> transactions = (Map<String, Object>) ordem.get("transactions");
                        if (transactions != null && transactions.containsKey("payments")) {
                            List<Map<String, Object>> payments = (List<Map<String, Object>>) transactions.get("payments");
                            if (payments != null && !payments.isEmpty()) {
                                Map<String, Object> pagamento = payments.get(0);
                                if (pagamento.get("status") != null) {
                                    statusOrdem = (String) pagamento.get("status");
                                }
                                if (pagamento.get("status_detail") != null) {
                                    statusDetail = (String) pagamento.get("status_detail");
                                }
                            }
                        }
                    }

                    System.out.println("STATUS MAPEADO DA ORDEM: " + statusOrdem + " | DETALHE: " + statusDetail);

                    // Verifica se foi aprovado/processado com sucesso
                    boolean aprovado = "processed".equalsIgnoreCase(statusOrdem) ||
                            "closed".equalsIgnoreCase(statusOrdem) ||
                            "approved".equalsIgnoreCase(statusOrdem);

                    boolean creditado = "accredited".equalsIgnoreCase(statusDetail) || aprovado;

                    String statusMapeado = (aprovado && creditado) ? "approved" : "pending";

                    return Map.of(
                            "status", statusMapeado,
                            "raw_status", statusOrdem != null ? statusOrdem : "unknown",
                            "order_id", ordem.get("id")
                    );
                }
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Erro ao consultar status da ordem: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Erro inesperado ao consultar status: " + e.getMessage());
        }

        return Map.of("status", "pending");
    }
}

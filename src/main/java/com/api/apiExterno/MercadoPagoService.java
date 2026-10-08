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
    private final String accessToken = "APP_USR-5785880777708319-100810-b063e8c14310df0b6562abaade0a4a37-1327545801";

    // Armazena temporariamente o orderId vinculado à externalReference para consulta ultra-rápida por ID
    private final Map<String, String> cacheExternalParaOrderId = new HashMap<>();

    public ResponseEntity<Map> criarOrdemPagamento(BigDecimal valor, String tipoPagamento, String externalReference) {
        String url = "https://api.mercadopago.com/v1/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

        Map<String, Object> body = new HashMap<>();
        body.put("type", "point");
        body.put("external_reference", externalReference);
        body.put("description", "Videira Natural - Point");

        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("amount", valor.toString());

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", List.of(paymentMap));
        body.put("transactions", transactions);

        Map<String, Object> pointConfig = new HashMap<>();
        pointConfig.put("terminal_id", "NEWLAND_N950__N950NCC804149944");

        Map<String, Object> paymentMethodConfig = new HashMap<>();
        paymentMethodConfig.put("default_type", tipoPagamento);

        Map<String, Object> config = new HashMap<>();
        config.put("point", pointConfig);
        config.put("payment_method", paymentMethodConfig);
        body.put("config", config);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> resposta = restTemplate.postForEntity(url, entity, Map.class);

            // Se criou com sucesso, já salvamos o ID da ordem vinculado à externalReference
            if (resposta.getStatusCode().is2xxSuccessful() && resposta.getBody() != null) {
                String orderIdCriado = (String) resposta.getBody().get("id");
                if (orderIdCriado != null) {
                    cacheExternalParaOrderId.put(externalReference, orderIdCriado);
                }
            }

            return resposta;
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getResponseBodyAsString()));
        }
    }

    public Map<String, Object> consultarStatusPorReferencia(String externalReference) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            Map<String, Object> ordem = null;

            // TENTATIVA 1: Se já tivermos o orderId salvo em memória, consulta DIRETO por ID (igualzinho funcionou no Postman!)
            String orderIdSalvo = cacheExternalParaOrderId.get(externalReference);
            if (orderIdSalvo != null) {
                String urlPorId = "https://api.mercadopago.com/v1/orders/" + orderIdSalvo;
                try {
                    ResponseEntity<Map> respId = restTemplate.exchange(urlPorId, HttpMethod.GET, entity, Map.class);
                    if (respId.getStatusCode().is2xxSuccessful() && respId.getBody() != null) {
                        ordem = respId.getBody();
                    }
                } catch (Exception ignored) {
                    // Se falhar a consulta por ID, prossegue para a busca por reference
                }
            }

            // TENTATIVA 2: Se não tiver o ID cacheado ou falhou, busca por external_reference (/search)
            if (ordem == null) {
                String urlSearch = "https://api.mercadopago.com/v1/orders/search?external_reference=" + externalReference;
                ResponseEntity<Map> response = restTemplate.exchange(urlSearch, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<Map<String, Object>> elements = (List<Map<String, Object>>) response.getBody().get("elements");
                    if (elements != null && !elements.isEmpty()) {
                        ordem = elements.get(0);
                        // Guarda o ID para as próximas checagens do loop
                        String foundId = (String) ordem.get("id");
                        if (foundId != null) {
                            cacheExternalParaOrderId.put(externalReference, foundId);
                        }
                    }
                }
            }

            // SE ENCONTROU A ORDEM (por ID ou por Search), VALIDA O STATUS
            if (ordem != null) {
                String statusOrdem = (String) ordem.get("status");
                String statusDetail = (String) ordem.get("status_detail");

                System.out.println("STATUS DA ORDEM: " + statusOrdem + " | DETALHE: " + statusDetail);

                boolean aprovado = "processed".equalsIgnoreCase(statusOrdem) ||
                        "closed".equalsIgnoreCase(statusOrdem) ||
                        "approved".equalsIgnoreCase(statusOrdem);

                boolean recusado = "rejected".equalsIgnoreCase(statusOrdem) ||
                        "cancelled".equalsIgnoreCase(statusOrdem) ||
                        "rejected".equalsIgnoreCase(statusDetail);

                boolean creditado = "accredited".equalsIgnoreCase(statusDetail) || aprovado;

                String statusMapeado = "pending";
                if (aprovado) {
                    statusMapeado = "approved";
                } else if (recusado) {
                    statusMapeado = "rejected";
                }

                return Map.of(
                        "status", statusMapeado,
                        "raw_status", statusOrdem != null ? statusOrdem : "unknown",
                        "order_id", ordem.get("id")
                );
            }

        } catch (HttpClientErrorException e) {
            System.err.println("Erro ao consultar status da ordem: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Erro inesperado ao consultar status: " + e.getMessage());
        }

        return Map.of("status", "pending");
    }
}
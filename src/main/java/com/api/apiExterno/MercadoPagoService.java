package com.api.apiExterno;

import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.entity.Pedido;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MercadoPagoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String accessToken = "APP_USR-5785880777708319-100810-b063e8c14310df0b6562abaade0a4a37-1327545801";

    // Armazena temporariamente o orderId vinculado à externalReference para consulta ultra-rápida por ID
    private final Map<String, String> cacheExternalParaOrderId = new HashMap<>();

    public ResponseEntity<Map> criarOrdemPagamento(BigDecimal valor, String tipoPagamento, Integer parcela, String externalReference) {
        String url = "https://api.mercadopago.com/v1/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

        Map<String, Object> body = new HashMap<>();
        body.put("type", "point");
        body.put("external_reference", externalReference);
        body.put("expiration_time", "PT1M");
        body.put("description", "Videira Natural - Point");

        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("amount", valor.toString());

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", List.of(paymentMap));
        body.put("transactions", transactions);

        Map<String, Object> pointConfig = new HashMap<>();
        pointConfig.put("terminal_id", "NEWLAND_N950__N950NCC804149944");
        pointConfig.put("print_on_terminal", "no_ticket");

        Map<String, Object> paymentMethodConfig = new HashMap<>();
        if (tipoPagamento.equals("credit_card")){
            paymentMethodConfig.put("default_type", tipoPagamento);
            paymentMethodConfig.put("default_installments", parcela);
            paymentMethodConfig.put("installments_cost", "seller");
        }

        Map<String, Object> config = new HashMap<>();
        config.put("point", pointConfig);
        config.put("payment_method", paymentMethodConfig);
        body.put("config", config);

        System.out.println(body);

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

    /**
     * Gera a preferência de pagamento online e retorna o link (init_point) real
     */


    public String criarPreferenciaCheckout(
            Pedido pedido,
            List<ItemCarrinhoRequest> itens) {

        String url = "https://api.mercadopago.com/checkout/preferences";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            List<Map<String, Object>> itemsList = new ArrayList<>();

            // 1. Adiciona cada produto individualmente
            if (itens != null) {
                for (ItemCarrinhoRequest item : itens) {

                    if (item == null) {
                        continue;
                    }

                    BigDecimal quantidade = item.getQuantidade() != null
                            ? item.getQuantidade()
                            : BigDecimal.ONE;

                    BigDecimal preco = item.getPreco() != null
                            ? item.getPreco()
                            : BigDecimal.ZERO;

                    if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    if (preco.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                "Preço de produto inválido.");
                    }

                    // A API de preferências utiliza quantidade inteira.
                    // Para quantidade fracionária, converte para uma unidade
                    // com o preço total do item.
                    boolean quantidadeInteira =
                            quantidade.stripTrailingZeros().scale() <= 0;

                    int quantidadeMP;
                    BigDecimal precoMP;

                    if (quantidadeInteira) {
                        quantidadeMP = quantidade.intValueExact();
                        precoMP = preco.setScale(
                                2, java.math.RoundingMode.HALF_UP);
                    } else {
                        quantidadeMP = 1;
                        precoMP = preco.multiply(quantidade).setScale(
                                2, java.math.RoundingMode.HALF_UP);
                    }

                    Map<String, Object> produto = new HashMap<>();

                    String nome = item.getNome() != null
                            && !item.getNome().isBlank()
                            ? item.getNome()
                            : "Produto Videira Natural";

                    produto.put("title", nome);
                    produto.put("quantity", quantidadeMP);
                    produto.put("unit_price", precoMP);
                    produto.put("currency_id", "BRL");

                    itemsList.add(produto);
                }
            }

            // 2. Obtém o frete
            BigDecimal frete = pedido.getValorFrete() != null
                    ? pedido.getValorFrete().setScale(
                    2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            if (frete.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Valor de frete inválido.");
            }

            // 3. Adiciona o frete como um item independente
            if (frete.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> itemFrete = new HashMap<>();

                itemFrete.put("title", "Frete / Taxa de Entrega");
                itemFrete.put("quantity", 1);
                itemFrete.put("unit_price", frete);
                itemFrete.put("currency_id", "BRL");

                itemsList.add(itemFrete);
            }

            // 4. Não cria preferência vazia
            if (itemsList.isEmpty()) {
                throw new IllegalArgumentException(
                        "O pedido não possui produtos ou frete para cobrar.");
            }

            // 5. Monta a preferência
            Map<String, Object> body = new HashMap<>();

            body.put("items", itemsList);
            body.put("external_reference", String.valueOf(pedido.getId()));

            Map<String, String> backUrls = new HashMap<>();
            backUrls.put("success",
                    "https://videiranatural.com.br/news");
            backUrls.put("pending",
                    "https://videiranatural.com.br/news");
            backUrls.put("failure",
                    "https://videiranatural.com.br/falha");

            body.put("back_urls", backUrls);

            // Registra exatamente o que será enviado
            System.out.println(
                    "Itens enviados ao Mercado Pago: " + itemsList);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                Map responseBody = response.getBody();

                System.out.println(
                        "ID da preferência Mercado Pago: "
                                + responseBody.get("id"));

                System.out.println(
                        "Link de pagamento: "
                                + responseBody.get("init_point"));

                Object initPoint = responseBody.get("init_point");

                if (initPoint instanceof String
                        && !((String) initPoint).isBlank()) {
                    return (String) initPoint;
                }
            }

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println(
                    "Erro Mercado Pago: "
                            + e.getStatusCode()
                            + " - "
                            + e.getResponseBodyAsString());

        } catch (Exception e) {
            System.err.println(
                    "Erro ao criar preferência: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private String produtoDescricaoPadrao(ItemCarrinhoRequest item) {
        return item.getNome() != null ? item.getNome() : "Produto Videira Natural";
    }

}
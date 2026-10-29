package com.api.apiExterno.mercadoPago;

import com.api.business.PedidoService;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.eNum.StatusPedido;
import com.api.entity.Pedido;
import com.api.repository.PedidoRepository;
import com.api.uteis.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class MercadoPagoService {

    @Autowired
    private Util restTemplate;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Value("${mercadopago.access.token}")
    private String accessToken;

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
        body.put("expiration_time", "PT2M");
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

        if (tipoPagamento.equals("CARTAO_CREDITO")){
            tipoPagamento = "credit_card";
            paymentMethodConfig.put("default_installments", parcela);
            paymentMethodConfig.put("installments_cost", "seller");
        }
        if (tipoPagamento.equals("CARTAO_DEBITO")){
            tipoPagamento = "debit_card";
        }
        paymentMethodConfig.put("default_type", tipoPagamento);

        Map<String, Object> config = new HashMap<>();
        config.put("point", pointConfig);
        config.put("payment_method", paymentMethodConfig);
        body.put("config", config);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> resposta = restTemplate.restTemplate().postForEntity(url, entity, Map.class);

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
                    ResponseEntity<Map> respId = restTemplate.restTemplate().exchange(urlPorId, HttpMethod.GET, entity, Map.class);
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
                ResponseEntity<Map> response = restTemplate.restTemplate().exchange(urlSearch, HttpMethod.GET, entity, Map.class);

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
            // 5. Monta a preferência restringe os meios de pagamento
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

            // --- RESTRIÇÃO DE MEIOS DE PAGAMENTO ---
            Map<String, Object> paymentMethods = new HashMap<>();

            // Exclui tipos indesejados para garantir que aceite apenas Cartão de Crédito, Débito e carteiras associadas
            List<Map<String, String>> excludedPaymentTypes = new ArrayList<>();

            // Exclui boletos (ticket)
            excludedPaymentTypes.add(Map.of("id", "ticket"));
            // Exclui Pix e transferências bancárias (bank_transfer)
            excludedPaymentTypes.add(Map.of("id", "bank_transfer"));

            paymentMethods.put("excluded_payment_types", excludedPaymentTypes);

            // Se quiser limitar o número máximo de parcelas, por exemplo:
            paymentMethods.put("installments", 3);

            body.put("payment_methods", paymentMethods);
            // ---------------------------------------


            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.restTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                Map responseBody = response.getBody();

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

    public boolean verificarPixPago(Double valorEsperado, String horaAberturaModal) {

        // 1. Converte a hora que veio do Vue.js para Instant e ajusta o formato se necessário
        Instant inicioJanela = Instant.parse(horaAberturaModal);

        // (Opcional) Se quiser dar uma margem de segurança de alguns segundos antes, pode subtrair,
        // mas o ideal é usar exatamente o momento que abriu ou 30 segundos antes para garantir.
        String beginDateParam = OffsetDateTime.ofInstant(inicioJanela, ZoneId.of("UTC")).toString();

        // 2. Monta a URL dinamicamente injetando o begin_date exato do modal
        // Monta a URL de forma limpa por concatenação
        String url = "https://api.mercadopago.com/v1/payments/search"
                + "?sort=date_approved"
                + "&criteria=desc"
                + "&payment_method_id=pix"
                + "&begin_date=" + beginDateParam
                + "&limit=2";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.restTemplate().exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");

                for (Map<String, Object> pagamento : results) {
                    String status = (String) pagamento.get("status");
                    Double valorPago = Double.valueOf(pagamento.get("transaction_amount").toString());
                    String dateApprovedStr = (String) pagamento.get("date_approved");

                    // Valida status aprovado e valor exato
                    if ("approved".equalsIgnoreCase(status) && valorPago.equals(valorEsperado)) {

                        Instant dataAprovacaoPix = OffsetDateTime.parse(dateApprovedStr).toInstant();

                        // Garante que o pagamento ocorreu após o modal abrir e dentro de uma janela razoável
                        if (!dataAprovacaoPix.isBefore(inicioJanela)) {
                            return true; // Pagamento encontrado e validado!
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar Pix no Mercado Pago: " + e.getMessage());
        }
        return false;
    }


    //WEBHOOK PARA PAGAMENTOS ONLINE DE LINK DE PAGAMENTO.
    public void processarPagamentoAprovadoPorWebhook(String paymentId) {
        String urlPagamento = "https://api.mercadopago.com/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 1. Consulta os detalhes do pagamento aprovado na API do Mercado Pago
            ResponseEntity<Map> response = restTemplate.restTemplate().exchange(urlPagamento, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> paymentData = response.getBody();

                String status = (String) paymentData.get("status"); // Ex: "approved"
                String externalReference = (String) paymentData.get("external_reference"); // Este é o ID do Pedido que salvamos antes!

                // 2. Se o pagamento foi realmente aprovado
                if ("approved".equalsIgnoreCase(status) && externalReference != null) {
                    Long pedidoId = Long.valueOf(externalReference);

                    // 3. Localiza o pedido no seu banco de dados
                    Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);

                    if (pedido != null) {
                        // 4. Atualiza a etapa para PAGO (ou avança de acordo com a sua regra de negócio/delivery)
                        pedido.setEtapa(StatusPedido.PAGO);
                        pedidoRepository.save(pedido);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar o pagamento aprovado no webhook: " + e.getMessage());
        }
    }



    private String produtoDescricaoPadrao(ItemCarrinhoRequest item) {
        return item.getNome() != null ? item.getNome() : "Produto Videira Natural";
    }
}
package com.api.apiExterno;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.resources.payment.Payment;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@AllArgsConstructor
public class MercadoPagoService {

    public Payment criarPagamentoMaquininha(BigDecimal valor, String descricao, String deviceId) throws Exception {

        PaymentCreateRequest request =
                PaymentCreateRequest.builder()
                        .transactionAmount(valor)
                        .description(descricao)
                        .paymentMethodId("credit_card")
                        .capture(true)
                        .pointOfInteraction(
                                (com.mercadopago.client.payment.PaymentPointOfInteractionRequest) Map.of("type", "POS", "sub_type", "SMART_POS", "device_id", deviceId)
                        )
                        .build();

        PaymentClient client = new PaymentClient();

        return client.create(request);
    }
}

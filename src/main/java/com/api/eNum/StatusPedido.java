package com.api.eNum;

public enum StatusPedido {
    CRIADO,                // Pedido criado
    PENDENTE,              // Aguardando pagamento
    PAGO,                  // Pagamento confirmado
    EM_PREPARACAO,         // Separando / produzindo
    PRONTO_PARA_RETIRADA,  // Disponível para o cliente retirar
    ENVIADO,               // Saiu para entrega (se houver entrega)
    ENTREGUE,              // Finalizado
    CANCELADO
}

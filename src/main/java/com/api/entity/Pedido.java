package com.api.entity; // Ajuste para o seu pacote correto

import com.api.dto.EnderecoDTO;
import com.api.eNum.StatusPedido;
import com.api.eNum.TipoEntrega;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pedidos")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId; // Alterado para Long para bater com o ID do cliente
    private String clienteNome;
    private String whatsapp;
    private Long enderecoId;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cep;
    private String complemento;

    @Column(columnDefinition = "TEXT")
    private String produto; // Descrição ou JSON dos itens do carrinho

    // Campos financeiros e de controle adicionados para o PDV e Online
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal totalFinal;
    private BigDecimal valorFrete;

    private String canalVenda;
    // Ex: "PDV", "WHATSAPP_ONLINE"
    @Enumerated(EnumType.STRING)
    private TipoEntrega tipoEntrega;

    @Column(columnDefinition = "TEXT")
    private String linkPagamento; // Link gerado pelo Mercado Pago para o WhatsApp

    @Enumerated(EnumType.STRING)
    private StatusPedido etapa = StatusPedido.CRIADO; // Inicia como CRIADO ou AGUARDANDO_PAGAMENTO

    private LocalDateTime dataHora = LocalDateTime.now();
}
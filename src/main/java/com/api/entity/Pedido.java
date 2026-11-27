package com.api.entity; // Ajuste para o seu pacote correto

import com.api.dto.ClientePdvDTO;
import com.api.dto.EnderecoDTO;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.eNum.FormaPagamento;
import com.api.eNum.StatusPedido;
import com.api.eNum.TipoEntrega;
import com.api.eNum.TipoVenda;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_pedidos")
@Data
@ToString(exclude = "itens")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId; // Alterado para Long para bater com o ID do cliente
    private String clienteNome;
    private String whatsapp;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cep;
    private String complemento;

    @Column(columnDefinition = "TEXT")
    private String produto; // Descrição ou JSON dos itens do carrinho
    //private List<Long> produtoid;

    // Campos financeiros e de controle adicionados para o PDV e Online
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal totalFinal;
    private BigDecimal valorFrete;

    @Enumerated(EnumType.STRING)
    private TipoVenda canalVenda;

    @Enumerated(EnumType.STRING)
    private TipoEntrega tipoEntrega;

    @Column(columnDefinition = "TEXT")
    private String linkPagamento; // Link gerado pelo Mercado Pago para o WhatsApp

    @Enumerated(EnumType.STRING)
    private StatusPedido etapa = StatusPedido.CRIADO; // Inicia como CRIADO ou AGUARDANDO_PAGAMENTO

    private LocalDateTime dataHora = LocalDateTime.now();

    @OneToMany(mappedBy = "pedido",cascade = CascadeType.ALL)
    private List<ItemPedido> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    @Column(name = "codigo_cupom", length = 60)
    private String codigoCupom;
}
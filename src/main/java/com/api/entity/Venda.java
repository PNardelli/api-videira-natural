package com.api.entity;

import com.api.eNum.FormaPagamento;
import com.api.eNum.StatusPedido;
import com.api.eNum.TipoEntrega;
import com.api.eNum.TipoVenda;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tb_venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false) // Data da venda não muda nunca
    private LocalDateTime dataVenda = LocalDateTime.now();

    private LocalDateTime dataUltimaAtualizacao; // Para rastrear mudanças de status

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Inicializar com ZERO evita erros de cálculo (NullPointerException)
    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal frete = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private FormaPagamento metodoPagamento;

    @Enumerated(EnumType.STRING)
    private TipoVenda canalVenda; // PDV ou ONLINE

    @Enumerated(EnumType.STRING)
    private TipoEntrega tipoEntrega; // RETIRADA ou ENTREGA

    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    // Relacionamento de endereço (útil para o canal ONLINE)
    @ManyToOne
    @JoinColumn(name = "endereco_entrega_id")
    private Endereco enderecoEntrega;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    // Helper method para garantir que a data de atualização mude sempre
    @PreUpdate
    @PrePersist
    public void onUpdate() {
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }
}

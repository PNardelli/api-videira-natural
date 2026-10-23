package com.api.entity;

import com.api.eNum.StatusVale;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_vale_troca")
public class ValeTroca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "codigo_barras", nullable = false, length = 50)
    private String codigoBarra;

    @NotNull
    @Column(name = "venda_origem_id", nullable = false)
    private Long vendaOrigemId;

    @Column(name = "cliente_id", nullable = true)
    private Long clienteId;

    @NotNull
    @Column(name = "valor_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorOriginal;

    @NotNull
    @Column(name = "valor_saldo", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSaldo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusVale status = StatusVale.PENDENTE;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    // Construtor padrão exigido pelo JPA
    public ValeTroca() {
    }

    // Construtor auxiliar para facilitar a criação rápida no Service
    public ValeTroca(Long vendaOrigemId, Long clienteId, BigDecimal valorOriginal, int diasValidade) {
        this.vendaOrigemId = vendaOrigemId;
        this.clienteId = clienteId; // se for null, continua null (cliente balcão)
        this.valorOriginal = valorOriginal;
        this.valorSaldo = valorOriginal; // No início, o saldo é igual ao valor original
        this.status = StatusVale.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
        this.codigoBarra = gerarCodigoBarrasCurto();
    }

    // Método para gerar um código curto e seguro baseado em UUID (ex: VALE-A1B2C3)
    private String gerarCodigoBarrasCurto() {
        String uuidLimpo = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "V" + uuidLimpo.substring(0, 8); // Exemplo: V8F4C2E1A
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
        if (this.codigoBarra == null || this.codigoBarra.isEmpty()) {
            this.codigoBarra = gerarCodigoBarrasCurto();
        }
    }

    // ==========================================
    // GETTERS E SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public Long getVendaOrigemId() {
        return vendaOrigemId;
    }

    public void setVendaOrigemId(Long vendaOrigemId) {
        this.vendaOrigemId = vendaOrigemId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public BigDecimal getValorSaldo() {
        return valorSaldo;
    }

    public void setValorSaldo(BigDecimal valorSaldo) {
        this.valorSaldo = valorSaldo;
    }

    public StatusVale getStatus() {
        return status;
    }

    public void setStatus(StatusVale status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}

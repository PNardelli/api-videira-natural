package com.api.entity;

import com.api.eNum.StatusVale;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
    @Column(name = "codigo_cupom", nullable = false, unique = true, length = 50)
    private String codigoCupom;

    @Column(name = "codigo_barras", nullable = true, length = 50)
    private String codigoBarra;

    @Column(name = "venda_origem_id", nullable = true)
    private Long vendaOrigemId;

    @Column(name = "cliente_id", nullable = true)
    private Long clienteId;

    @Column(name = "origem", length = 30)
    private String origem; // Ex.: TROCA, CAMPEONATO, CAMPANHA, CREDITO

    @Column(name = "descricao", length = 150)
    private String descricao;

    @Column(name = "tipo_desconto", length = 30)
    private String tipoDesconto; // Ex.: PERCENTUAL, VALOR_FIXO

    @NotNull
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull
    @Column(name = "valor_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorOriginal;

    @NotNull
    @Column(name = "valor_saldo", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSaldo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusVale status = StatusVale.ATIVO;

    @Column(name = "data_validade")
    private LocalDateTime dataValidade;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public ValeTroca() {
    }

    // Construtor para troca/devolução vinculada a uma venda
    public ValeTroca(Long vendaOrigemId, Long clienteId, BigDecimal valorOriginal, int diasValidade) {
        this.vendaOrigemId = vendaOrigemId;
        this.clienteId = clienteId;
        this.origem = "TROCA";
        this.tipoDesconto = "VALOR_FIXO";
        this.valor = valorOriginal;
        this.valorOriginal = valorOriginal;
        this.valorSaldo = valorOriginal;
        this.status = StatusVale.ATIVO;
        this.dataCriacao = LocalDateTime.now();
        this.dataValidade = diasValidade > 0 ? this.dataCriacao.plusDays(diasValidade) : null;
        this.codigoBarra = gerarCodigoBarrasCurto();
        this.codigoCupom = this.codigoBarra;
    }

    // Construtor auxiliar para voucher/cupom avulso (ex.: campeonato / promoção sem venda de origem)
    public ValeTroca(String codigoPersonalizado, String origem, String descricao, String tipoDesconto, BigDecimal valor, Long clienteId, BigDecimal valorOriginal, int diasValidade) {
        this.vendaOrigemId = null;
        this.clienteId = clienteId;
        this.origem = (origem != null && !origem.isBlank()) ? origem.trim().toUpperCase() : "AVULSO";
        this.descricao = descricao;
        this.tipoDesconto = (tipoDesconto != null && !tipoDesconto.isBlank()) ? tipoDesconto.trim().toUpperCase() : "VALOR_FIXO";
        this.valor = valor;
        this.valorOriginal = valorOriginal != null ? valorOriginal : (valor != null ? valor : BigDecimal.ZERO);
        this.valorSaldo = this.valorOriginal;
        this.status = StatusVale.ATIVO;
        this.dataCriacao = LocalDateTime.now();
        this.dataValidade = diasValidade > 0 ? this.dataCriacao.plusDays(diasValidade) : null;
        this.codigoCupom = (codigoPersonalizado != null && !codigoPersonalizado.isBlank())
                ? codigoPersonalizado.trim().toUpperCase().replaceAll("\\s+", "")
                : gerarCodigoBarrasCurto();
        this.codigoBarra = gerarCodigoBarrasCurto();
    }

    private String gerarCodigoBarrasCurto() {
        String uuidLimpo = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "V" + uuidLimpo.substring(0, 8);
    }

    @PrePersist
    public void prePersist() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }

        if (this.codigoCupom != null && !this.codigoCupom.isBlank()) {
            this.codigoCupom = this.codigoCupom.trim().toUpperCase().replaceAll("\\s+", "");
        } else if (this.codigoBarra != null && !this.codigoBarra.isBlank()) {
            this.codigoCupom = this.codigoBarra.trim().toUpperCase().replaceAll("\\s+", "");
        }

        if (this.tipoDesconto != null && !this.tipoDesconto.isBlank()) {
            this.tipoDesconto = this.tipoDesconto.trim().toUpperCase();
        } else {
            this.tipoDesconto = "VALOR_FIXO";
        }

        if (this.valor == null && this.valorOriginal != null) {
            this.valor = this.valorOriginal;
        }

        if (this.valorOriginal == null) {
            this.valorOriginal = this.valor != null ? this.valor : BigDecimal.ZERO;
        }

        if (this.valorSaldo == null) {
            this.valorSaldo = this.valorOriginal;
        }

        if (this.status == null) {
            this.status = StatusVale.ATIVO;
        }

        if (this.origem == null || this.origem.isBlank()) {
            this.origem = this.vendaOrigemId != null ? "TROCA" : "AVULSO";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public void setCodigoCupom(String codigoCupom) {
        this.codigoCupom = codigoCupom;
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

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoDesconto() {
        return tipoDesconto;
    }

    public void setTipoDesconto(String tipoDesconto) {
        this.tipoDesconto = tipoDesconto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
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

    public LocalDateTime getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDateTime dataValidade) {
        this.dataValidade = dataValidade;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
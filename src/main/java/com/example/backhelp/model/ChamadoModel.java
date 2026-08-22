package com.example.backhelp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chamados")
public class ChamadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String protocolo; //

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusChamado status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Nivel nivelAtual;

    private LocalDateTime dataAbertura;
    private LocalDateTime prazoSla;
    private LocalDateTime dataFechamento;

    @Column(columnDefinition = "TEXT")
    private String relatorioEscalonamento;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private UsuarioModel solicitante;

    @ManyToOne
    @JoinColumn(name = "atendente_id")
    private UsuarioModel atendenteResponsavel;

    @PrePersist
    public void prePersist() {
        this.dataAbertura = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusChamado.ABERTO;
        }
        if (this.nivelAtual == null) {
            this.nivelAtual = Nivel.N1;
        }
        if (this.protocolo == null) {
            this.protocolo = "HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    public ChamadoModel() {
    }

    public ChamadoModel(Long id, String protocolo, String titulo, String descricao, Categoria categoria, Urgencia urgencia, StatusChamado status, Nivel nivelAtual, LocalDateTime dataAbertura, LocalDateTime prazoSla, LocalDateTime dataFechamento, String relatorioEscalonamento, UsuarioModel solicitante, UsuarioModel atendenteResponsavel) {
        this.id = id;
        this.protocolo = protocolo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.urgencia = urgencia;
        this.status = status;
        this.nivelAtual = nivelAtual;
        this.dataAbertura = dataAbertura;
        this.prazoSla = prazoSla;
        this.dataFechamento = dataFechamento;
        this.relatorioEscalonamento = relatorioEscalonamento;
        this.solicitante = solicitante;
        this.atendenteResponsavel = atendenteResponsavel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public Nivel getNivelAtual() {
        return nivelAtual;
    }

    public void setNivelAtual(Nivel nivelAtual) {
        this.nivelAtual = nivelAtual;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getPrazoSla() {
        return prazoSla;
    }

    public void setPrazoSla(LocalDateTime prazoSla) {
        this.prazoSla = prazoSla;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public String getRelatorioEscalonamento() {
        return relatorioEscalonamento;
    }

    public void setRelatorioEscalonamento(String relatorioEscalonamento) {
        this.relatorioEscalonamento = relatorioEscalonamento;
    }

    public UsuarioModel getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(UsuarioModel solicitante) {
        this.solicitante = solicitante;
    }

    public UsuarioModel getAtendenteResponsavel() {
        return atendenteResponsavel;
    }

    public void setAtendenteResponsavel(UsuarioModel atendenteResponsavel) {
        this.atendenteResponsavel = atendenteResponsavel;
    }
}

package com.example.backhelp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_chamados")
public class ChamadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String protocolo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgencia urgencia;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    private String caminhoAnexo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusChamado status = StatusChamado.ABERTO;

    @Enumerated(EnumType.STRING)
    private Perfil nivelAtendimento; // ATENDENTE_N1, ATENDENTE_N2 ou ATENDENTE_N3

    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataLimiteSla;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioModel usuarioAbertura;

    @ManyToOne
    @JoinColumn(name = "atendente_id")
    private UsuarioModel atendenteResponsavel;

    @Column(columnDefinition = "TEXT")
    private String solucao;

    // Método automático para calcular o prazo do SLA ao salvar o chamado
    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        if (this.urgencia != null) {
            this.dataLimiteSla = this.dataCriacao.plusHours(this.urgencia.getHorasSla());
        }
    }

    public ChamadoModel() {
    }

    public ChamadoModel(Long id, String protocolo, Categoria categoria, Urgencia urgencia, String descricao, String caminhoAnexo, StatusChamado status, Perfil nivelAtendimento, LocalDateTime dataCriacao, LocalDateTime dataFinalizacao, LocalDateTime dataLimiteSla, UsuarioModel usuarioAbertura, UsuarioModel atendenteResponsavel, String solucao) {
        this.id = id;
        this.protocolo = protocolo;
        this.categoria = categoria;
        this.urgencia = urgencia;
        this.descricao = descricao;
        this.caminhoAnexo = caminhoAnexo;
        this.status = status;
        this.nivelAtendimento = nivelAtendimento;
        this.dataCriacao = dataCriacao;
        this.dataFinalizacao = dataFinalizacao;
        this.dataLimiteSla = dataLimiteSla;
        this.usuarioAbertura = usuarioAbertura;
        this.atendenteResponsavel = atendenteResponsavel;
        this.solucao = solucao;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoAnexo() {
        return caminhoAnexo;
    }

    public void setCaminhoAnexo(String caminhoAnexo) {
        this.caminhoAnexo = caminhoAnexo;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public Perfil getNivelAtendimento() {
        return nivelAtendimento;
    }

    public void setNivelAtendimento(Perfil nivelAtendimento) {
        this.nivelAtendimento = nivelAtendimento;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public LocalDateTime getDataLimiteSla() {
        return dataLimiteSla;
    }

    public void setDataLimiteSla(LocalDateTime dataLimiteSla) {
        this.dataLimiteSla = dataLimiteSla;
    }

    public UsuarioModel getUsuarioAbertura() {
        return usuarioAbertura;
    }

    public void setUsuarioAbertura(UsuarioModel usuarioAbertura) {
        this.usuarioAbertura = usuarioAbertura;
    }

    public UsuarioModel getAtendenteResponsavel() {
        return atendenteResponsavel;
    }

    public void setAtendenteResponsavel(UsuarioModel atendenteResponsavel) {
        this.atendenteResponsavel = atendenteResponsavel;
    }

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }
}
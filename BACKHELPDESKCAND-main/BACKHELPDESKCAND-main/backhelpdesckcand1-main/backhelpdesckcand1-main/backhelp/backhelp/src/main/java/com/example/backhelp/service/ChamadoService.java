package com.example.backhelp.service;

import com.example.backhelp.dto.ChamadoRequestDTO;
import com.example.backhelp.dto.ChamadoResponseDTO;
import com.example.backhelp.dto.DashboardDTO;
import com.example.backhelp.model.*;
import com.example.backhelp.repository.ChamadoRepository;
import com.example.backhelp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO dto) {
        UsuarioModel usuario = usuarioRepository.findById(dto.usuarioAberturaId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!usuario.isEmailConfirmado()) {
            throw new IllegalStateException("Abertura bloqueada até a confirmação do e-mail corporativo.");
        }

        if (dto.caminhoAnexo() != null && !dto.caminhoAnexo().isBlank() && !validarAnexo(dto.caminhoAnexo())) {
            throw new IllegalArgumentException("Extensão de anexo inválida. Formatos permitidos: .pdf, .svg, .png e .jpg.");
        }

        ChamadoModel chamado = new ChamadoModel();
        chamado.setCategoria(dto.categoria());
        chamado.setUrgencia(dto.urgencia());
        chamado.setDescricao(dto.descricao());
        chamado.setCaminhoAnexo(dto.caminhoAnexo());
        chamado.setUsuarioAbertura(usuario);
        chamado.setStatus(StatusChamado.ABERTO);

        if (String.valueOf(dto.categoria()).equalsIgnoreCase("HARDWARE")) {
            chamado.setNivelAtendimento(Perfil.TECNICO_N2);
        } else {
            chamado.setNivelAtendimento(Perfil.ATENDENTE_N1);
        }

        ChamadoModel salvo = chamadoRepository.save(chamado);
        return toDTO(salvo);
    }

    public ChamadoResponseDTO escalonarChamado(Long chamadoId, Perfil novoNivel) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado."));

        if (isDowngrade(chamado.getNivelAtendimento(), novoNivel)) {
            throw new IllegalArgumentException("Proibido rebaixar o nível de atendimento de um chamado.");
        }

        chamado.setNivelAtendimento(novoNivel);
        return toDTO(chamadoRepository.save(chamado));
    }

    public ChamadoResponseDTO atenderEConverter(Long chamadoId, Long atendenteId, StatusChamado novoStatus, String solucao) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado."));
        UsuarioModel atendente = usuarioRepository.findById(atendenteId)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado."));

        chamado.setAtendenteResponsavel(atendente);
        chamado.setStatus(novoStatus);

        if (novoStatus == StatusChamado.FECHADO) {
            chamado.setDataFinalizacao(LocalDateTime.now());
            chamado.setSolucao(solucao);
        }

        return toDTO(chamadoRepository.save(chamado));
    }

    public List<ChamadoResponseDTO> listarComFiltros(StatusChamado status, Perfil nivel, Urgencia urgencia) {
        List<ChamadoModel> chamados;

        if (status != null) {
            chamados = chamadoRepository.findByStatus(status);
        } else if (nivel != null) {
            chamados = chamadoRepository.findByNivelAtendimento(nivel);
        } else if (urgencia != null) {
            chamados = chamadoRepository.findByUrgencia(urgencia);
        } else {
            chamados = chamadoRepository.findAll();
        }

        atualizarStatusSla(chamados);

        return chamados.stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ChamadoResponseDTO> listarTodos() {
        List<ChamadoModel> chamados = chamadoRepository.findAll();
        atualizarStatusSla(chamados);
        return chamados.stream().map(this::toDTO).toList();
    }

    public DashboardDTO obterMetricsDashboard() {
        List<ChamadoModel> todos = chamadoRepository.findAll();
        atualizarStatusSla(todos);

        long abertos = chamadoRepository.countByStatus(StatusChamado.ABERTO);
        long resolvidos = chamadoRepository.countByStatus(StatusChamado.FECHADO);
        long atrasados = chamadoRepository.countByStatus(StatusChamado.ATRASADO);

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        long hoje = chamadoRepository.countByDataCriacaoBetween(inicioDia, fimDia);

        return new DashboardDTO(abertos, resolvidos, atrasados, hoje);
    }

    private void atualizarStatusSla(List<ChamadoModel> chamados) {
        LocalDateTime agora = LocalDateTime.now();
        for (ChamadoModel chamado : chamados) {
            if (chamado.getStatus() != StatusChamado.FECHADO &&
                    chamado.getDataLimiteSla() != null &&
                    agora.isAfter(chamado.getDataLimiteSla())) {
                chamado.setStatus(StatusChamado.ATRASADO);
                chamadoRepository.save(chamado);
            }
        }
    }

    private boolean validarAnexo(String caminho) {
        String lower = caminho.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".svg") || lower.endsWith(".png") || lower.endsWith(".jpg");
    }

    private boolean isDowngrade(Perfil atual, Perfil novo) {
        if (atual == null || novo == null) return false;
        return novo.ordinal() < atual.ordinal();
    }

    private ChamadoResponseDTO toDTO(ChamadoModel model) {
        return new ChamadoResponseDTO(
                model.getId(),
                model.getProtocolo(),
                model.getCategoria(),
                model.getUrgencia(),
                model.getDescricao(),
                model.getCaminhoAnexo(),
                model.getStatus(),
                model.getNivelAtendimento(),
                model.getDataCriacao(),
                model.getDataLimiteSla(),
                model.getUsuarioAbertura() != null ? model.getUsuarioAbertura().getEmail() : null,
                model.getAtendenteResponsavel() != null ? model.getAtendenteResponsavel().getEmail() : null,
                model.getSolucao()
        );
    }
}
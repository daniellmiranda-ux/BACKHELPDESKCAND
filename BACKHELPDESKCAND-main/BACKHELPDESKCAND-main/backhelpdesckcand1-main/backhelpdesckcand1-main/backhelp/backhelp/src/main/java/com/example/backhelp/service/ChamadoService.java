package com.example.backhelp.service;

import com.example.backhelp.dto.ChamadoRequestDTO;
import com.example.backhelp.dto.ChamadoResponseDTO;
import com.example.backhelp.dto.DashboardDTO;
import com.example.backhelp.model.*;
import com.example.backhelp.repository.ChamadoRepository;
import com.example.backhelp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO dto) {
        UsuarioModel usuario = usuarioRepository.findById(dto.usuarioAberturaId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

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

        chamado.setNivelAtendimento(
                "HARDWARE".equalsIgnoreCase(String.valueOf(dto.categoria()))
                        ? Perfil.ATENDENTE_N2
                        : Perfil.ATENDENTE_N1
        );

        ChamadoModel salvo = chamadoRepository.save(chamado);
        return toDTO(salvo);
    }

    @Transactional
    public ChamadoResponseDTO escalonarChamado(Long chamadoId, Perfil novoNivel) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado."));

        if (novoNivel == null || chamado.getNivelAtendimento() == null || novoNivel.ordinal() <= chamado.getNivelAtendimento().ordinal()) {
            throw new IllegalArgumentException("Proibido rebaixar ou manter o mesmo nível de atendimento em um escalonamento.");
        }

        chamado.setNivelAtendimento(novoNivel);
        return toDTO(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponseDTO atenderEConverter(Long chamadoId, Long atendenteId, StatusChamado novoStatus, String solucao) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado."));
        UsuarioModel atendente = usuarioRepository.findById(atendenteId)
                .orElseThrow(() -> new IllegalArgumentException("Atendente não encontrado."));

        chamado.setAtendenteResponsavel(atendente);
        chamado.setStatus(novoStatus);

        if (novoStatus == StatusChamado.FECHADO) {
            chamado.setDataFinalizacao(LocalDateTime.now());
            chamado.setSolucao(solucao);
        }

        return toDTO(chamadoRepository.save(chamado));
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> listarComFiltros(StatusChamado status, Perfil nivel, Urgencia urgencia) {
        List<ChamadoModel> chamados = chamadoRepository.findAll();

        return chamados.stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> nivel == null || c.getNivelAtendimento() == nivel)
                .filter(c -> urgencia == null || c.getUrgencia() == urgencia)
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> listarTodos() {
        return chamadoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public DashboardDTO obterMetricsDashboard() {
        List<ChamadoModel> todos = chamadoRepository.findAll();

        long atrasados = todos.stream().filter(ChamadoModel::isAtrasado).count();
        long resolvidos = todos.stream().filter(c -> c.getStatus() == StatusChamado.FECHADO).count();
        long abertos = todos.stream().filter(c -> c.getStatus() == StatusChamado.ABERTO && !c.isAtrasado()).count();

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        long hoje = chamadoRepository.countByDataCriacaoBetween(inicioDia, fimDia);

        return new DashboardDTO(abertos, resolvidos, atrasados, hoje);
    }

    private boolean validarAnexo(String caminho) {
        String lower = caminho.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".svg") || lower.endsWith(".png") || lower.endsWith(".jpg");
    }

    private ChamadoResponseDTO toDTO(ChamadoModel model) {
        StatusChamado statusExibicao = model.isAtrasado() ? StatusChamado.ATRASADO : model.getStatus();

        return new ChamadoResponseDTO(
                model.getId(),
                model.getProtocolo(),
                model.getCategoria(),
                model.getUrgencia(),
                model.getDescricao(),
                model.getCaminhoAnexo(),
                statusExibicao,
                model.getNivelAtendimento(),
                model.getDataCriacao(),
                model.getDataLimiteSla(),
                model.getUsuarioAbertura() != null ? model.getUsuarioAbertura().getEmail() : null,
                model.getAtendenteResponsavel() != null ? model.getAtendenteResponsavel().getEmail() : null,
                model.getSolucao()
        );
    }
}
package com.example.backhelp.service;

import com.example.backhelp.dto.ChamadoRequestDTO;
import com.example.backhelp.dto.ChamadoResponseDTO;
import com.example.backhelp.dto.DashboardDTO;
import com.example.backhelp.model.*;
import com.example.backhelp.repository.ChamadoRepository;
import com.example.backhelp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ChamadoService(ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO dto, String emailUsuario) {
        UsuarioModel usuario = usuarioRepository.findByEmail(emailUsuario)
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
    public ChamadoResponseDTO salvarAnexo(Long chamadoId, MultipartFile file) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + chamadoId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado não pode estar vazio.");
        }

        try {
            Path pathDiretorio = Paths.get(uploadDir);
            if (!Files.exists(pathDiretorio)) {
                Files.createDirectories(pathDiretorio);
            }

            String nomeOriginal = file.getOriginalFilename();
            String extensao = "";
            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }

            String nomeArquivo = UUID.randomUUID().toString() + extensao;
            Path caminhoCompleto = pathDiretorio.resolve(nomeArquivo);

            Files.copy(file.getInputStream(), caminhoCompleto, StandardCopyOption.REPLACE_EXISTING);

            chamado.setCaminhoAnexo(caminhoCompleto.toString());
            ChamadoModel atualizado = chamadoRepository.save(chamado);

            return toDTO(atualizado);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar anexo fotográfico: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> carregarAnexo(Long id) {
        ChamadoModel chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + id));

        if (chamado.getCaminhoAnexo() == null || chamado.getCaminhoAnexo().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(chamado.getCaminhoAnexo());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar anexo: " + e.getMessage());
        }
    }

    @Transactional
    public ChamadoResponseDTO escalonarChamado(Long chamadoId, Perfil novoNivel) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado."));

        validarEscalonamento(chamado.getNivelAtendimento(), novoNivel);

        chamado.setNivelAtendimento(novoNivel);
        return toDTO(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponseDTO atenderEConverter(Long chamadoId, Long atendenteId, StatusChamado novoStatus, String solucao) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado."));

        UsuarioModel atendente = usuarioRepository.findById(atendenteId)
                .orElseThrow(() -> new IllegalArgumentException("Atendente não encontrado."));

        if (atendente.getPerfil() == Perfil.USUARIO_COMUM || atendente.getPerfil() == Perfil.SETOR_ADMINISTRATIVO) {
            throw new IllegalArgumentException("Apenas atendentes técnicos (N1, N2, N3) podem assumir chamados.");
        }

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
        return chamadoRepository.buscarComFiltros(status, nivel, urgencia).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> listarTodos() {
        return chamadoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public DashboardDTO obterMetricsDashboard() {
        LocalDateTime agora = LocalDateTime.now();

        long atrasados = chamadoRepository.countAtrasados(agora);
        long resolvidos = chamadoRepository.countByStatus(StatusChamado.FECHADO);
        long abertos = chamadoRepository.countAbertosNaoAtrasados(agora);

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        long hoje = chamadoRepository.countByDataCriacaoBetween(inicioDia, fimDia);

        return new DashboardDTO(abertos, resolvidos, atrasados, hoje);
    }

    private void validarEscalonamento(Perfil atual, Perfil novo) {
        if (novo == null || (novo != Perfil.ATENDENTE_N1 && novo != Perfil.ATENDENTE_N2 && novo != Perfil.ATENDENTE_N3)) {
            throw new IllegalArgumentException("O nível de destino deve ser um perfil de atendente válido (N1, N2 ou N3).");
        }

        if (atual == Perfil.ATENDENTE_N3) {
            throw new IllegalArgumentException("Proibido escalonar: O chamado já está no nível máximo (N3).");
        }

        if (atual == Perfil.ATENDENTE_N1 && novo != Perfil.ATENDENTE_N2 && novo != Perfil.ATENDENTE_N3) {
            throw new IllegalArgumentException("Proibido rebaixar nível: N1 só pode avançar para N2 ou N3.");
        }

        if (atual == Perfil.ATENDENTE_N2 && novo != Perfil.ATENDENTE_N3) {
            throw new IllegalArgumentException("Proibido rebaixar nível: N2 só pode avançar para N3.");
        }
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
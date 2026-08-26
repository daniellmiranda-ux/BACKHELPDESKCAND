package com.example.backhelp.service;

import com.example.backhelp.dto.LoginRequestDTO;
import com.example.backhelp.dto.UsuarioRequestDTO;
import com.example.backhelp.dto.UsuarioResponseDTO;
import com.example.backhelp.model.UsuarioModel;
import com.example.backhelp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        // Validação estrita de domínio corporativo (RN01, RNF03)
        if (dto.email() == null || !dto.email().endsWith("@helpdeskcand.com")) {
            throw new IllegalArgumentException("Apenas e-mails do domínio @helpdeskcand.com são permitidos.");
        }
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        usuario.setSetor(dto.setor());
        usuario.setCargo(dto.cargo());
        usuario.setPerfil(dto.perfil());
        usuario.setEmailConfirmado(false);

        UsuarioModel salvo = usuarioRepository.save(usuario);
        return toDTO(salvo);
    }

    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        UsuarioModel usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!usuario.getSenha().equals(dto.senha())) {
            throw new RuntimeException("Senha incorreta.");
        }

        return toDTO(usuario);
    }

    public UsuarioResponseDTO confirmarEmail(Long id) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        usuario.setEmailConfirmado(true);
        return toDTO(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::toDTO).toList();
    }

    private UsuarioResponseDTO toDTO(UsuarioModel model) {
        return new UsuarioResponseDTO(
                model.getId(),
                model.getEmail(),
                model.getSetor(),
                model.getCargo(),
                model.getPerfil(),
                model.isEmailConfirmado()
        );
    }
}
package com.example.backhelp.service;

import com.example.backhelp.dto.LoginRequestDTO;
import com.example.backhelp.dto.TokenResponseDTO;
import com.example.backhelp.dto.UsuarioRequestDTO;
import com.example.backhelp.dto.UsuarioResponseDTO;
import com.example.backhelp.model.UsuarioModel;
import com.example.backhelp.repository.UsuarioRepository;
import com.example.backhelp.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        if (dto.email() == null || !dto.email().endsWith("@helpdeskcand.com")) {
            throw new IllegalArgumentException("Apenas e-mails do domínio @helpdeskcand.com são permitidos.");
        }
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setSetor(dto.setor());
        usuario.setCargo(dto.cargo());
        usuario.setPerfil(dto.perfil());
        usuario.setEmailConfirmado(false);

        UsuarioModel salvo = usuarioRepository.save(usuario);
        return toDTO(salvo);
    }

    public UsuarioResponseDTO editarUsuario(Long id, UsuarioRequestDTO dto) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (dto.email() != null) {
            if (!dto.email().endsWith("@helpdeskcand.com")) {
                throw new IllegalArgumentException("Apenas e-mails do domínio @helpdeskcand.com são permitidos.");
            }
            if (!usuario.getEmail().equals(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("E-mail já cadastrado.");
            }
            usuario.setEmail(dto.email());
        }
        if (dto.setor() != null) usuario.setSetor(dto.setor());
        if (dto.cargo() != null) usuario.setCargo(dto.cargo());
        if (dto.perfil() != null) usuario.setPerfil(dto.perfil());

        UsuarioModel atualizado = usuarioRepository.save(usuario);
        return toDTO(atualizado);
    }

    public TokenResponseDTO login(LoginRequestDTO dto) {
        UsuarioModel usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta.");
        }

        String token = tokenProvider.gerarToken(usuario.getEmail(), usuario.getPerfil().name());
        return new TokenResponseDTO(token, "Bearer", toDTO(usuario));
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
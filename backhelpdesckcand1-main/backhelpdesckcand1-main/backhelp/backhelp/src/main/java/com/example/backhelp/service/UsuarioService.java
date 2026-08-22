package com.example.backhelp.service;

import com.example.backhelp.model.UsuarioModel;
import com.example.backhelp.repository.UsuarioRepository;
import DTO.Request.UsuarioRequestDto;
import DTO.Response.UsuarioResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método auxiliar: Transforma o Model do Banco no DTO seguro para o Front-end
    private UsuarioResponseDto converterParaDto(UsuarioModel model) {
        return new UsuarioResponseDto(
                model.getId(),
                model.getNome(),
                model.getEmail(),
                model.getCargo(),
                model.getSetor(),
                model.getPerfil(),
                model.getNivel(),
                model.isAtivo()
        );
    }

    // Cria um usuário recebendo apenas os dados permitidos
    public UsuarioResponseDto salvar(UsuarioRequestDto dto) {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha()); // Em um sistema real, você criptografaria aqui
        usuario.setCargo(dto.cargo());
        usuario.setSetor(dto.setor());
        usuario.setPerfil(dto.perfil());
        usuario.setNivel(dto.nivel());
        usuario.setAtivo(true); // Ativo por padrão na criação

        UsuarioModel usuarioSalvo = usuarioRepository.save(usuario);

        return converterParaDto(usuarioSalvo);
    }

    // Retorna todos os usuários já filtrados sem a senha
    public List<UsuarioResponseDto> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    // Busca um usuário específico
    public UsuarioResponseDto buscarPorId(Long id) {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return converterParaDto(usuario);
    }
}
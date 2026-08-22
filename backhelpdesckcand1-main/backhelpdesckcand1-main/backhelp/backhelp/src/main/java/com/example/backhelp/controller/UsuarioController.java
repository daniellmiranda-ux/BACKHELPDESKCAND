package com.example.backhelp.controller;

import DTO.Request.UsuarioRequestDto;
import DTO.Response.UsuarioResponseDto;
import com.example.backhelp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> criarUsuario(@RequestBody UsuarioRequestDto dto) {
        UsuarioResponseDto novoUsuario = usuarioService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
        List<UsuarioResponseDto> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> buscarUsuarioPorId(@PathVariable Long id) {
        UsuarioResponseDto usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }
}
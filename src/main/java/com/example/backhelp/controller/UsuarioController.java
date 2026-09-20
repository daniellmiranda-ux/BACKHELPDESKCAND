package com.example.backhelp.controller;

import com.example.backhelp.dto.LoginRequestDTO;
import com.example.backhelp.dto.TokenResponseDTO;
import com.example.backhelp.dto.UsuarioRequestDTO;
import com.example.backhelp.dto.UsuarioResponseDTO;
import com.example.backhelp.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/usuarios", "/usuarios"})
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAuthority('SETOR_ADMINISTRATIVO')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            return ResponseEntity.ok(usuarioService.login(dto));
        } catch (Exception e) {
            log.error("==========================================================");
            log.error("[UsuarioController] ERRO CRÍTICO NO LOGIN:", e);
            log.error("==========================================================");
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SETOR_ADMINISTRATIVO')")
    public ResponseEntity<UsuarioResponseDTO> editarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.editarUsuario(id, dto));
    }

    @PutMapping("/{id}/confirmar-email")
    public ResponseEntity<UsuarioResponseDTO> confirmarEmail(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.confirmarEmail(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SETOR_ADMINISTRATIVO')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }
}
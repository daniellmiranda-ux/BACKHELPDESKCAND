package com.example.backhelp.controller;

import com.example.backhelp.dto.ChamadoRequestDTO;
import com.example.backhelp.dto.ChamadoResponseDTO;
import com.example.backhelp.dto.DashboardDTO;
import com.example.backhelp.model.Perfil;
import com.example.backhelp.model.StatusChamado;
import com.example.backhelp.service.ChamadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(@RequestBody ChamadoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.criarChamado(dto));
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarTodos());
    }

    @PutMapping("/{id}/escalonar")
    public ResponseEntity<?> escalonarChamado(
            @PathVariable Long id,
            @RequestParam Perfil novoNivel) {
        try {
            ChamadoResponseDTO chamadoAtualizado = chamadoService.escalonarChamado(id, novoNivel);
            return ResponseEntity.ok(chamadoAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/atender")
    public ResponseEntity<?> atenderEConverter(
            @PathVariable Long id,
            @RequestParam Long atendenteId,
            @RequestParam StatusChamado status,
            @RequestBody(required = false) String solucao) {
        try {
            ChamadoResponseDTO chamadoAtualizado = chamadoService.atenderEConverter(id, atendenteId, status, solucao);
            return ResponseEntity.ok(chamadoAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> obterDashboard() {
        return ResponseEntity.ok(chamadoService.obterMetricsDashboard());
    }
}
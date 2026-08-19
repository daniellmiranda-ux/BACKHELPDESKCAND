package com.example.backhelp.controller;

import com.example.backhelp.model.ChamadoModel;
import com.example.backhelp.model.Nivel;
import com.example.backhelp.service.ChamadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chamados")
@CrossOrigin(origins = "*")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<?> abrirChamado(@RequestBody ChamadoModel chamado) {
        try {
            ChamadoModel novoChamado = chamadoService.abrirChamado(chamado);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoChamado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/escalonar")
    public ResponseEntity<?> escalonarChamado(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            Nivel novoNivel = Nivel.valueOf(payload.get("novoNivel"));
            String relatorio = payload.get("relatorio");

            ChamadoModel chamadoAtualizado = chamadoService.escalonarChamado(id, novoNivel, relatorio);
            return ResponseEntity.ok(chamadoAtualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ChamadoModel>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarTodos());
    }
}

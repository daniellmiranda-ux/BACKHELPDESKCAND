package com.example.backhelp.service;

import com.example.backhelp.model.ChamadoModel;
import com.example.backhelp.model.Nivel;
import com.example.backhelp.model.StatusChamado;
import com.example.backhelp.repository.ChamadoRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }


    public ChamadoModel abrirChamado(ChamadoModel chamado) {
        List<StatusChamado> statusFinalizados = Arrays.asList(StatusChamado.CONCLUIDO, StatusChamado.FECHADO);

        long chamadosAbertos = chamadoRepository.countBySolicitanteIdAndStatusNotIn(
                chamado.getSolicitante().getId(),
                statusFinalizados
        );

        if (chamadosAbertos >= 3) {
            throw new RuntimeException("RN03: O usuário já possui 3 chamados abertos simultaneamente.");
        }

        return chamadoRepository.save(chamado);
    }


    public ChamadoModel escalonarChamado(Long chamadoId, Nivel novoNivel, String relatorio) {
        ChamadoModel chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado com o ID: " + chamadoId));

        if (relatorio == null || relatorio.trim().isEmpty()) {
            throw new RuntimeException("RN06: É obrigatório preencher o relatório justificando o escalonamento.");
        }


        if (novoNivel.ordinal() <= chamado.getNivelAtual().ordinal()) {
            throw new RuntimeException("RN05: Proibido rebaixar ou manter o nível atual do chamado.");
        }

        chamado.setNivelAtual(novoNivel);
        chamado.setRelatorioEscalonamento(relatorio);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);

        return chamadoRepository.save(chamado);
    }

    public List<ChamadoModel> listarTodos() {
        return chamadoRepository.findAll();
    }
}

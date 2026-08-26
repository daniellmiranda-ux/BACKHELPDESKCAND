package com.example.backhelp.dto;

import com.example.backhelp.model.Categoria;
import com.example.backhelp.model.Urgencia;

public record ChamadoRequestDTO(
        Categoria categoria,
        Urgencia urgencia,
        String descricao,
        String caminhoAnexo,
        Long usuarioAberturaId
) {}
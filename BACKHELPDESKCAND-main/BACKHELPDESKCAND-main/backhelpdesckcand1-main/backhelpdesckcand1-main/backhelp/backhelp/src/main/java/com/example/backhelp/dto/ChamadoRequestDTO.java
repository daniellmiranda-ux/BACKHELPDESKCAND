package com.example.backhelp.dto;

import com.example.backhelp.model.Categoria;
import com.example.backhelp.model.Urgencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChamadoRequestDTO(

        @NotNull(message = "A categoria do chamado é obrigatória.")
        Categoria categoria,

        @NotNull(message = "A urgência do chamado é obrigatória.")
        Urgencia urgencia,

        @NotBlank(message = "A descrição do chamado não pode estar vazia.")
        String descricao,

        String caminhoAnexo
) {}
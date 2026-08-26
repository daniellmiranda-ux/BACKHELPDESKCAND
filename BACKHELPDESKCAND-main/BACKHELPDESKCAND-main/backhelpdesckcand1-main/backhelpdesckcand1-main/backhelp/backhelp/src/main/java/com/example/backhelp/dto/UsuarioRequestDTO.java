package com.example.backhelp.dto;

import com.example.backhelp.model.Perfil;

public record UsuarioRequestDTO(
        String email,
        String senha,
        String setor,
        String cargo,
        Perfil perfil
) {}
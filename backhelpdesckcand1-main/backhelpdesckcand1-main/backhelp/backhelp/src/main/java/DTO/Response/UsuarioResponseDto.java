package DTO.Response;

import com.example.backhelp.model.Nivel;
import com.example.backhelp.model.Perfil;

public record UsuarioResponseDto(
        Long id,
        String nome,
        String email,
        String cargo,
        String setor,
        Perfil perfil,
        Nivel nivel,
        boolean ativo
) {
}
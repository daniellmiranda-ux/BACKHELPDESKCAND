package DTO.Request;

import com.example.backhelp.model.Nivel;
import com.example.backhelp.model.Perfil;

public record UsuarioRequestDto(
        String nome,
        String email,
        String senha,
        String cargo,
        String setor,
        Perfil perfil,
        Nivel nivel
) {
}
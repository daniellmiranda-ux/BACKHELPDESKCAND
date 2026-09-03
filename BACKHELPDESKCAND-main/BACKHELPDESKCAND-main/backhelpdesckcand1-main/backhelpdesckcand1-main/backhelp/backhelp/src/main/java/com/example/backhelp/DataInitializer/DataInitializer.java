package com.example.backhelp.config;

import com.example.backhelp.model.Perfil;
import com.example.backhelp.model.UsuarioModel;
import com.example.backhelp.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByEmail("admin@helpdeskcand.com").isEmpty()) {
                UsuarioModel admin = new UsuarioModel();
                admin.setEmail("admin@helpdeskcand.com");
                admin.setSenha(passwordEncoder.encode("123"));
                admin.setSetor("TI");
                admin.setCargo("Administrador");
                admin.setPerfil(Perfil.SETOR_ADMINISTRATIVO);
                admin.setEmailConfirmado(true);
                usuarioRepository.save(admin);
            }
        };
    }
}
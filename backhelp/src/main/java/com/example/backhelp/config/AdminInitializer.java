package com.example.backhelp.config;

import com.example.backhelp.model.Perfil;
import com.example.backhelp.model.UsuarioModel;
import com.example.backhelp.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@helpdeskcand.com";

        // Procura o utilizador existente ou cria um novo se não existir
        UsuarioModel admin = usuarioRepository.findByEmail(adminEmail)
                .orElseGet(UsuarioModel::new);

        admin.setEmail(adminEmail);
        admin.setSenha(passwordEncoder.encode("admin123")); // Força a atualização da senha para a hash BCrypt correta
        admin.setSetor("Administração");
        admin.setCargo("Administrador do Sistema");
        admin.setPerfil(Perfil.SETOR_ADMINISTRATIVO);
        admin.setEmailConfirmado(true);

        usuarioRepository.save(admin);

        log.info("==================================================================");
        log.info("[AdminInitializer] Administrador sincronizado e atualizado com sucesso!");
        log.info("E-mail: {}", adminEmail);
        log.info("Senha padrão configurada: admin123");
        log.info("Perfil: SETOR_ADMINISTRATIVO");
        log.info("==================================================================");
    }
}
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

        if (!usuarioRepository.existsByEmail(adminEmail)) {
            UsuarioModel admin = new UsuarioModel();
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setSetor("Administração");
            admin.setCargo("Administrador do Sistema");
            admin.setPerfil(Perfil.SETOR_ADMINISTRATIVO);
            admin.setEmailConfirmado(true);

            usuarioRepository.save(admin);

            log.info("==================================================================");
            log.info("[AdminInitializer] Usuário Administrador inicial criado com sucesso!");
            log.info("E-mail: {}", adminEmail);
            log.info("Senha padrão: admin123");
            log.info("Perfil: SETOR_ADMINISTRATIVO");
            log.info("==================================================================");
        } else {
            log.info("[AdminInitializer] Usuário Administrador ({}) já existente no banco de dados.", adminEmail);
        }
    }
}

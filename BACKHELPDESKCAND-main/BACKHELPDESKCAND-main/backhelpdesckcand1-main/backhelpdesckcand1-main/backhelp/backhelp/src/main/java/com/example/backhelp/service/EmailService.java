package com.example.backhelp.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailConfirmacao(String destinatario, String tokenOuId) {
        SimpleMailMessage email = new SimpleMailMessage();

        email.setTo(destinatario);
        email.setSubject("Confirme seu cadastro - Sistema HelpDesk");

        String link = "http://localhost:8080/usuarios/confirmar?id=" + tokenOuId;

        email.setText("Olá!\n\nPor favor, clique no link abaixo para confirmar seu e-mail e liberar seu acesso ao sistema:\n" + link);

        mailSender.send(email);
    }
}
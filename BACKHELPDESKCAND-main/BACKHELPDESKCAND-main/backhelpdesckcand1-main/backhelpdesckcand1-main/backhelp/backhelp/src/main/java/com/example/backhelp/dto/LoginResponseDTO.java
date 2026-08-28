package com.example.backhelp.dto;

public record LoginResponseDTO(String token, String tipo) {
    public LoginResponseDTO(String token) {
        this(token, "Bearer");
    }
}
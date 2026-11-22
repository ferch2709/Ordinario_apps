package com.example.fakewhatsapp.auth;

import java.util.Map;

public class AuthRequest {
    private String email;
    private String password;
    private Map<String, Object> data;

    // Constructor para Login
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Constructor para Registro (incluye el nombre en 'data')
    public AuthRequest(String email, String password, Map<String, Object> data) {
        this.email = email;
        this.password = password;
        this.data = data;
    }
}
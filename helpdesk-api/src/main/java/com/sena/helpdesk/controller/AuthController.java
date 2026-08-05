package com.sena.helpdesk.controller;

import com.sena.helpdesk.dto.*;
import com.sena.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Ruta pública: verifica que la API está viva
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("mensaje", "pong"));
    }

    // Ruta pública: registra un usuario nuevo con rol USUARIO
    @PostMapping("/auth/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request) {
        AuthResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Ruta pública: autentica y devuelve accessToken + refreshToken
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Ruta pública: recibe un refreshToken válido y devuelve un nuevo accessToken
    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refrescar(request);
        return ResponseEntity.ok(response);
    }

    // Ruta protegida (cualquier usuario logueado): revoca el refreshToken enviado
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request, Authentication authentication) {
        authService.logout(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}

package com.sena.helpdesk.controller;

import com.sena.helpdesk.dto.PromoverSoporteRequest;
import com.sena.helpdesk.dto.UsuarioResponse;
import com.sena.helpdesk.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint 7.3 del taller: solo ADMIN (protegido en SecurityConfig con
 * /api/admin/**).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/soporte")
    public ResponseEntity<UsuarioResponse> promoverASoporte(@Valid @RequestBody PromoverSoporteRequest request) {
        return ResponseEntity.ok(adminService.promoverASoporte(request.email()));
    }
}
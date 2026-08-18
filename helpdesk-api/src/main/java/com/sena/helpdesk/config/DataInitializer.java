package com.sena.helpdesk.config;

import com.sena.helpdesk.model.Rol;
import com.sena.helpdesk.model.Usuario;
import com.sena.helpdesk.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Siembra un usuario ADMIN por defecto (admin@correo.com / admin123) para
 * poder ejercitar los endpoints de administración, ya que el registro
 * público solo crea usuarios con rol USUARIO (punto 7.1 del taller).
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            if (!usuarioRepository.existsByEmail("admin@correo.com")) {
                usuarioRepository.save(Usuario.builder()
                        .nombre("Administrador")
                        .email("admin@correo.com")
                        .password(passwordEncoder.encode("admin123"))
                        .rol(Rol.ADMIN)
                        .build());
            }
        };
    }
}
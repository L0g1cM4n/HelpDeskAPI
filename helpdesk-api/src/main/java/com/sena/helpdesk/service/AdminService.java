package com.sena.helpdesk.service;

import com.sena.helpdesk.dto.UsuarioResponse;
import com.sena.helpdesk.exception.RecursoNoEncontradoException;
import com.sena.helpdesk.model.Rol;
import com.sena.helpdesk.model.Usuario;
import com.sena.helpdesk.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioResponse promoverASoporte(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el email " + email));

        usuario.setRol(Rol.SOPORTE);

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}
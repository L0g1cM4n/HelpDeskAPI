package com.sena.helpdesk.repository;

import com.sena.helpdesk.model.RefreshToken;
import com.sena.helpdesk.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUsuarioAndRevocadoFalse(Usuario usuario);
}

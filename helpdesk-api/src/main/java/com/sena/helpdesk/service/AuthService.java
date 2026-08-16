package com.sena.helpdesk.service;

import com.sena.helpdesk.dto.*;
import com.sena.helpdesk.exception.AccesoDenegadoException;
import com.sena.helpdesk.exception.EmailYaRegistradoException;
import com.sena.helpdesk.exception.RefreshTokenInvalidoException;
import com.sena.helpdesk.model.RefreshToken;
import com.sena.helpdesk.model.Rol;
import com.sena.helpdesk.model.Usuario;
import com.sena.helpdesk.repository.RefreshTokenRepository;
import com.sena.helpdesk.repository.UsuarioRepository;
import com.sena.helpdesk.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailYaRegistradoException(request.email());
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // nunca texto plano
                .rol(Rol.USUARIO) // todo registro público entra como USUARIO
                .build();

        usuarioRepository.save(usuario);

        return generarParDeTokens(usuario);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Si la contraseña no coincide, AuthenticationManager lanza BadCredentialsException,
        // que el GlobalExceptionHandler convierte en un 401.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email o contraseña incorrectos"));

        return generarParDeTokens(usuario);
    }

    @Transactional
    public TokenResponse refrescar(RefreshRequest request) {
        String tokenValor = request.refreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValor)
                .orElseThrow(() -> new RefreshTokenInvalidoException("El refreshToken no existe"));

        if (refreshToken.isRevocado()) {
            throw new RefreshTokenInvalidoException("El refreshToken fue revocado");
        }
        if (refreshToken.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenInvalidoException("El refreshToken expiró");
        }

        Usuario usuario = refreshToken.getUsuario();
        String nuevoAccessToken = jwtService.generarAccessToken(usuario);

        return new TokenResponse(nuevoAccessToken);
    }

    @Transactional
    public void logout(String emailUsuarioAutenticado, RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new RefreshTokenInvalidoException("El refreshToken no existe"));

        // Un usuario solo puede revocar sus propios tokens de sesión
        if (!refreshToken.getUsuario().getEmail().equals(emailUsuarioAutenticado)) {
            throw new AccesoDenegadoException("No puede revocar un refreshToken que no le pertenece");
        }

        refreshToken.setRevocado(true);
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse generarParDeTokens(Usuario usuario) {
        String accessToken = jwtService.generarAccessToken(usuario);
        String refreshTokenValor = jwtService.generarRefreshTokenValor(usuario);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValor)
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000))
                .revocado(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenValor);
    }
}

package com.sena.helpdesk.security;

import com.sena.helpdesk.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Encargado exclusivamente de crear y leer JWT firmados.
 * No sabe nada de base de datos ni de HTTP — solo de tokens.
 *
 * El accessToken lleva el email (subject) y el rol como claim, tal como
 * pide el punto 8.2 del documento del taller. El refreshToken solo lleva
 * el email; su control de validez/revocación vive en la tabla RefreshToken
 * (ver RefreshTokenRepository), no en el propio JWT.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generarAccessToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .claim("tipo", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Genera el VALOR del refresh token (un JWT firmado, usado solo como
     * identificador opaco). Quien decide si sigue siendo válido es la
     * entidad RefreshToken en base de datos (Opción A del taller), no
     * la expiración de este JWT en sí.
     */
    public String generarRefreshTokenValor(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("tipo", "refresh")
                // jti único: sin esto, dos tokens emitidos en el mismo segundo
                // (iat tiene precisión de segundos) quedan idénticos y chocan con
                // el constraint UNIQUE de refresh_tokens.token al hacer login.
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    /**
     * Devuelve el claim "tipo" (access/refresh). El filtro de autenticación
     * exige tipo == "access": un refresh token NO debe servir para acceder
     * a rutas protegidas (punto 8.8 del documento del taller).
     */
    public String extraerTipo(String token) {
        return extraerClaim(token, claims -> claims.get("tipo", String.class));
    }

    public boolean esTokenValido(String token, String emailEsperado) {
        try {
            String email = extraerEmail(token);
            return email.equals(emailEsperado) && !estaExpirado(token);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}

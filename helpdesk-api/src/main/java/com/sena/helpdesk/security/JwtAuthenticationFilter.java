package com.sena.helpdesk.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Se ejecuta una vez por cada petición HTTP. Busca el header
 * "Authorization: Bearer <accessToken>", lo valida, y si es correcto,
 * deja al usuario autenticado en el SecurityContext para el resto del
 * pipeline de Spring Security (de ahí lo toman @PreAuthorize, etc.).
 *
 * Si no hay token, o es inválido, simplemente deja pasar la petición sin
 * autenticar: es SecurityConfig (más adelante) quien decide si esa ruta
 * requiere autenticación y responde 401 si hace falta.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);

        if (header == null || !header.startsWith(PREFIJO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIJO.length());

        try {
            String email = jwtService.extraerEmail(token);

            boolean noHayAutenticacionAun = SecurityContextHolder.getContext().getAuthentication() == null;

            if (email != null && noHayAutenticacionAun) {
                UserDetails usuario = userDetailsService.loadUserByUsername(email);

                if (jwtService.esTokenValido(token, usuario.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            usuario, null, usuario.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Token corrupto, expirado o firma inválida: no se autentica.
            // SecurityConfig responderá 401 si la ruta lo requiere.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

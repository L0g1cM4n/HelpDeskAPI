package com.sena.helpdesk.config;

import com.sena.helpdesk.security.JwtAccessDeniedHandler;
import com.sena.helpdesk.security.JwtAuthenticationEntryPoint;
import com.sena.helpdesk.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en los controladores
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless con JWT: no se usan formularios ni CSRF de sesión
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 7.1 Rutas públicas
                        .requestMatchers(
                                "/api/auth/registro",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/ping",
                                "/h2-console/**"
                        ).permitAll()

                        // 7.3 Rutas protegidas por rol específico
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("GET", "/api/tickets").hasAnyRole("SOPORTE", "ADMIN")
                        .requestMatchers("GET", "/api/tickets/vencidos").hasAnyRole("SOPORTE", "ADMIN")
                        .requestMatchers("PATCH", "/api/tickets/*/estado").hasAnyRole("SOPORTE", "ADMIN")

                        // 7.2 Cualquier otra ruta bajo /api requiere estar autenticado
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                // Necesario para poder ver la consola H2 embebida en un <frame>
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler)           // 403
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt, tal como exige el punto 8.1 del documento del taller
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

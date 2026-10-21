
package com.laura.chatbot_ia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Centraliza las reglas de acceso y el cifrado de contraseñas.
@Configuration
public class SecurityConfig {

    // Permite registrar e iniciar sesión sin token y protege las demás rutas.
    // Configura la API para autenticar cada petición mediante JWT, sin sesiones.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService) throws Exception {

        // Desactiva CSRF porque la API utiliza autenticación mediante Bearer JWT.
        http.csrf(csrf -> csrf.disable());

        // Evita que Spring Security cree o utilice sesiones HTTP.
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Permite el registro y el login; exige autenticación para las demás rutas.
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/register",
                        "/api/auth/login")
                .permitAll()
                .anyRequest().authenticated());

        // Registra nuestro filtro JWT antes del filtro de usuario y contraseña.
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtService),
                UsernamePasswordAuthenticationFilter.class);

        // Construye la cadena de filtros con todas las reglas configuradas.
        return http.build();
    }

    // Registra BCrypt para guardar y verificar contraseñas de forma segura.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


package com.laura.chatbot_ia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Centraliza las reglas de acceso y el cifrado de contraseñas.
@Configuration
public class SecurityConfig {

    // Permite registrar cuentas sin autenticación y protege las demás rutas.
    // Define las reglas de acceso para la API REST.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Desactiva CSRF porque la API se preparará para autenticación JWT sin sesión.
        http.csrf(csrf -> csrf.disable());

        // Permite el registro público y exige autenticación para el resto de rutas.
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .anyRequest().authenticated());

        // Construye la cadena de filtros con las reglas anteriores.
        return http.build();
    }

    // Registra BCrypt para guardar y verificar contraseñas de forma segura.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

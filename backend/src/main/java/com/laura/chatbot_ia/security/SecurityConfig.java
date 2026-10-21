
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

    // Abre el registro, el login y el chat público, y protege las demás rutas.
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

        // Registrar e iniciar sesión no exige un token previo.
        // El chat identifica el negocio en la ruta y atiende a cualquier cliente.
        // El resto de rutas, incluidas las demás de /api/businesses, exigen token.
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/register",
                        "/api/auth/login")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/businesses/*/chat")
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

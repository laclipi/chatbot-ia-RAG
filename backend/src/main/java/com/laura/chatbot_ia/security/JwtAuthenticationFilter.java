
package com.laura.chatbot_ia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Intercepta las peticiones HTTP para autenticar al usuario mediante JWT.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // Recibe el servicio que comprueba la firma y la vigencia del token.
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Examina el encabezado Authorization antes de continuar con la petición.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Obtiene las credenciales Bearer enviadas por el cliente.
        String authorization = request.getHeader("Authorization");

        // Si no hay un Bearer token, continúa sin autenticar por JWT.
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Elimina el prefijo Bearer para obtener el JWT.
        String token = authorization.substring(7);

        try {
            // Verifica la firma y extrae el email incluido en el token.
            Claims claims = jwtService.extractClaims(token);
            String email = claims.getSubject();

            // Comprueba que el token pertenece al usuario indicado y no ha caducado.
            if (email != null && jwtService.isTokenValid(token, email)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Registra al usuario autenticado en el contexto de Spring Security.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of());

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (JwtException | IllegalArgumentException exception) {
            // Un token inválido no autentica al usuario.
            SecurityContextHolder.clearContext();
        }

        // Continúa con la cadena de filtros de Spring Security.
        filterChain.doFilter(request, response);
    }
}

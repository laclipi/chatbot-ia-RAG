
package com.laura.chatbot_ia.controller;

// Indica que esta clase recibe peticiones HTTP y devuelve respuestas REST.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Registra esta clase como controlador REST de Spring Boot.
@RestController

// Todas las rutas de este controlador empezarán por /api.
@RequestMapping("/api")
public class HealthController {

    // Atiende las peticiones GET dirigidas a /api/health.
    @GetMapping("/health")
    public String health() {
        // Devuelve una respuesta sencilla para confirmar que la API responde.
        return "Chatbot IA API is running";
    }
}

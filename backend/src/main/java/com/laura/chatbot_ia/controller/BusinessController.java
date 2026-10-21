
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.exception.AuthenticatedUserNotFoundException;
import com.laura.chatbot_ia.service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

// Indica que esta clase recibe peticiones HTTP y devuelve respuestas REST.
@RestController

// Todas las rutas de este controlador empiezan por /api/businesses.
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    // Spring inyecta el servicio automáticamente.
    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // GET /api/businesses
    // Devuelve solo los negocios del usuario autenticado.
    @GetMapping
    public List<Business> findAll(Authentication authentication) {
        return businessService.findAll(authenticatedEmail(authentication));
    }

    // GET /api/businesses/{id}
    // Exige pertenencia antes de devolver el negocio.
    @GetMapping("/{id}")
    public ResponseEntity<Business> findById(
            @PathVariable Long id,
            Authentication authentication) {
        return businessService.findById(id, authenticatedEmail(authentication))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/businesses
    // Crea el negocio y lo vincula al email autenticado por el JWT.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Business create(
            @Valid @RequestBody Business business,
            Authentication authentication) {
        return businessService.create(business, authenticatedEmail(authentication));
    }

    // El filtro JWT guarda el email del token como principal de la petición.
    // Una autenticación ausente o anónima no identifica a un usuario de la aplicación.
    private String authenticatedEmail(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AuthenticatedUserNotFoundException();
        }

        return authentication.getName();
    }
}

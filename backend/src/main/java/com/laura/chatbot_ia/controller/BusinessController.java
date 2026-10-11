
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    // Devuelve la lista de negocios registrados.
    @GetMapping
    public List<Business> findAll() {
        return businessService.findAll();
    }

    // POST /api/businesses
    // Recibe un negocio en formato JSON y lo guarda en PostgreSQL.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Business create(@RequestBody Business business) {
        return businessService.save(business);
    }
}

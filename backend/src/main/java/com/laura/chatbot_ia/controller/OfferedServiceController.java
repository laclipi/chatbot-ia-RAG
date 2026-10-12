
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.service.OfferedServiceManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/services")
public class OfferedServiceController {

    private final OfferedServiceManager offeredServiceManager;

    public OfferedServiceController(
            OfferedServiceManager offeredServiceManager) {
        this.offeredServiceManager = offeredServiceManager;
    }

    // Lista los servicios asociados al negocio indicado.
    @GetMapping
    public List<OfferedService> findByBusinessId(
            @PathVariable Long businessId) {
        return offeredServiceManager.findByBusinessId(businessId);
    }

    // Crea un servicio para el negocio indicado.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferedService create(
            @PathVariable Long businessId,
            @Valid @RequestBody OfferedService offeredService) {
        return offeredServiceManager.create(businessId, offeredService);
    }
}

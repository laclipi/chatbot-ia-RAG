
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

    // Obtiene un servicio concreto, verificando que pertenece al negocio indicado.
    @GetMapping("/id/{serviceId}")
    public OfferedService findByIdAndBusinessId(
            @PathVariable Long businessId,
            @PathVariable Long serviceId) {

        return offeredServiceManager.findByIdAndBusinessId(
                serviceId,
                businessId);
    }

    // Actualiza un servicio concreto del negocio indicado.
    @PutMapping("/{serviceId}")
    public OfferedService update(
            @PathVariable Long businessId,
            @PathVariable Long serviceId,
            @Valid @RequestBody OfferedService updatedService) {

        // Delegamos la validación y la actualización en la capa de servicio.
        return offeredServiceManager.update(
                businessId,
                serviceId,
                updatedService);
    }

    // Elimina un servicio concreto del negocio indicado.
    @DeleteMapping("/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long businessId,
            @PathVariable Long serviceId) {

        // Delegamos la eliminación y las comprobaciones en la capa de servicio.
        offeredServiceManager.delete(businessId, serviceId);
    }

    // Cambia el estado activo de un servicio sin modificar sus otros datos.
    @PatchMapping("/{serviceId}/active")
    public OfferedService changeActiveStatus(
            @PathVariable Long businessId,
            @PathVariable Long serviceId,
            @RequestParam boolean active) {

        // Delegamos la comprobación y el cambio de estado en el servicio.
        return offeredServiceManager.changeActiveStatus(
                businessId,
                serviceId,
                active);
    }

    // Devuelve únicamente los servicios activos del negocio indicado.
    @GetMapping("/active")
    public List<OfferedService> findActiveByBusinessId(
            @PathVariable Long businessId) {

        // Reutilizamos la lógica existente que filtra los servicios activos.
        return offeredServiceManager.findByBusinessId(businessId);
    }

}

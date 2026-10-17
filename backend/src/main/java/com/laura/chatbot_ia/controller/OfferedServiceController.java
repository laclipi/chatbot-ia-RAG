// Declaramos el paquete y agrupamos los imports de DTOs, entidades, servicios y Spring.
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.dto.OfferedServiceRequest;
import com.laura.chatbot_ia.dto.OfferedServiceResponse;
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

    // Devuelve los servicios del negocio mediante DTOs, sin exponer las entidades
    // JPA.
    @GetMapping
    public List<OfferedServiceResponse> findByBusinessId(
            @PathVariable Long businessId) {

        // Recuperamos los servicios aplicando las comprobaciones de la capa de
        // servicio.
        List<OfferedService> services = offeredServiceManager.findByBusinessId(businessId);

        // Convertimos cada entidad en un DTO para mantener uniforme la respuesta de la
        // API.
        return services.stream()
                .map(service -> {
                    OfferedServiceResponse response = new OfferedServiceResponse();
                    response.setId(service.getId());
                    response.setName(service.getName());
                    response.setDescription(service.getDescription());
                    response.setPrice(service.getPrice());
                    response.setDurationMinutes(service.getDurationMinutes());
                    response.setActive(service.isActive());
                    return response;
                })
                .toList();
    }

    // Crea un servicio para el negocio indicado.
    // Recibe los datos de entrada, crea la entidad y devuelve una respuesta sin
    // exponerla directamente.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferedServiceResponse create(
            @PathVariable Long businessId,
            @Valid @RequestBody OfferedServiceRequest request) {

        // Construimos la entidad a partir de los campos permitidos en la petición.
        OfferedService offeredService = new OfferedService();
        offeredService.setName(request.getName());
        offeredService.setDescription(request.getDescription());
        offeredService.setPrice(request.getPrice());
        offeredService.setDurationMinutes(request.getDurationMinutes());

        // La capa de servicio asigna el negocio y persiste la entidad.
        OfferedService createdService = offeredServiceManager.create(businessId, offeredService);

        // Convertimos la entidad persistida en el DTO que devolverá la API.
        OfferedServiceResponse response = new OfferedServiceResponse();
        response.setId(createdService.getId());
        response.setName(createdService.getName());
        response.setDescription(createdService.getDescription());
        response.setPrice(createdService.getPrice());
        response.setDurationMinutes(createdService.getDurationMinutes());
        response.setActive(createdService.isActive());

        return response;
    }

    // Obtiene un servicio concreto, verificando que pertenece al negocio indicado.

    // Consulta un servicio usando la misma estructura de ruta que PUT y DELETE.
    @GetMapping("/{serviceId}")
    public OfferedServiceResponse findByIdAndBusinessId(
            @PathVariable Long businessId,
            @PathVariable Long serviceId) {

        // Recuperamos la entidad aplicando las comprobaciones de pertenencia al
        // negocio.
        OfferedService service = offeredServiceManager.findByIdAndBusinessId(
                serviceId,
                businessId);

        // Convertimos la entidad en el formato de respuesta público de la API.
        OfferedServiceResponse response = new OfferedServiceResponse();
        response.setId(service.getId());
        response.setName(service.getName());
        response.setDescription(service.getDescription());
        response.setPrice(service.getPrice());
        response.setDurationMinutes(service.getDurationMinutes());
        response.setActive(service.isActive());

        return response;
    }

    // Actualiza un servicio concreto del negocio indicado.

    // Actualiza un servicio del negocio indicado usando un DTO de entrada y otro de
    // salida.
    @PutMapping("/{serviceId}")
    public OfferedServiceResponse update(
            @PathVariable Long businessId,
            @PathVariable Long serviceId,
            @Valid @RequestBody OfferedServiceRequest request) {

        // Construimos una entidad con los campos que el cliente puede modificar.
        OfferedService updatedService = new OfferedService();
        updatedService.setName(request.getName());
        updatedService.setDescription(request.getDescription());
        updatedService.setPrice(request.getPrice());
        updatedService.setDurationMinutes(request.getDurationMinutes());

        // La capa de servicio comprueba la pertenencia al negocio y aplica la
        // actualización.
        OfferedService service = offeredServiceManager.update(
                businessId,
                serviceId,
                updatedService);

        // Convertimos la entidad actualizada en el DTO de respuesta.
        OfferedServiceResponse response = new OfferedServiceResponse();
        response.setId(service.getId());
        response.setName(service.getName());
        response.setDescription(service.getDescription());
        response.setPrice(service.getPrice());
        response.setDurationMinutes(service.getDurationMinutes());
        response.setActive(service.isActive());

        return response;
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

    // Cambia el estado activo del servicio y devuelve la información mediante un
    // DTO.
    @PatchMapping("/{serviceId}/active")
    public OfferedServiceResponse changeActiveStatus(
            @PathVariable Long businessId,
            @PathVariable Long serviceId,
            @RequestParam boolean active) {

        // La capa de servicio comprueba la pertenencia al negocio y actualiza el
        // estado.
        OfferedService service = offeredServiceManager.changeActiveStatus(
                businessId,
                serviceId,
                active);

        // Transformamos la entidad actualizada en el formato público de respuesta.
        OfferedServiceResponse response = new OfferedServiceResponse();
        response.setId(service.getId());
        response.setName(service.getName());
        response.setDescription(service.getDescription());
        response.setPrice(service.getPrice());
        response.setDurationMinutes(service.getDurationMinutes());
        response.setActive(service.isActive());

        return response;
    }

    // Devuelve únicamente los servicios activos del negocio indicado.

    // Devuelve los servicios activos del negocio usando el DTO de respuesta de la
    // API.
    @GetMapping("/active")
    public List<OfferedServiceResponse> findActiveByBusinessId(
            @PathVariable Long businessId) {

        // Recuperamos los servicios activos mediante la lógica existente de la capa de
        // servicio.
        List<OfferedService> services = offeredServiceManager.findByBusinessId(businessId);

        // Convertimos las entidades recuperadas en DTOs antes de devolverlas al
        // cliente.
        return services.stream()
                .map(service -> {
                    OfferedServiceResponse response = new OfferedServiceResponse();
                    response.setId(service.getId());
                    response.setName(service.getName());
                    response.setDescription(service.getDescription());
                    response.setPrice(service.getPrice());
                    response.setDurationMinutes(service.getDurationMinutes());
                    response.setActive(service.isActive());
                    return response;
                })
                .toList();
    }

}

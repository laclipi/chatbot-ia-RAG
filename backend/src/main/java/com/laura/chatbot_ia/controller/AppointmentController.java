
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.dto.AppointmentRequest;
import com.laura.chatbot_ia.dto.AppointmentResponse;
import com.laura.chatbot_ia.entity.Appointment;
import com.laura.chatbot_ia.exception.AuthenticatedUserNotFoundException;
import com.laura.chatbot_ia.service.AppointmentService;
import com.laura.chatbot_ia.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Expone el endpoint REST para crear reservas y delega la lógica al servicio.
@RestController
@RequestMapping("/api/businesses/{businessId}/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final BusinessService businessService;

    // Spring inyecta el servicio de reservas y la comprobación de pertenencia.
    public AppointmentController(
            AppointmentService appointmentService,
            BusinessService businessService) {
        this.appointmentService = appointmentService;
        this.businessService = businessService;
    }

    // Valida el JSON recibido y devuelve HTTP 201 cuando la reserva se crea.
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @PathVariable Long businessId,
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication) {

        // La pertenencia se comprueba antes de crear una reserva en este negocio.
        businessService.requireMembership(authenticatedEmail(authentication), businessId);

        Appointment appointment = appointmentService.create(businessId, request);

        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Devuelve las reservas del negocio indicado con HTTP 200.
    @GetMapping
    public ResponseEntity<java.util.List<AppointmentResponse>> findByBusinessId(
            @PathVariable Long businessId,
            Authentication authentication) {

        // La pertenencia se comprueba antes de leer las reservas de este negocio.
        businessService.requireMembership(authenticatedEmail(authentication), businessId);

        java.util.List<AppointmentResponse> responses = appointmentService
                .findByBusinessId(businessId)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // Cancela una reserva concreta sin eliminar su registro de la base de datos.
    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable Long businessId,
            @PathVariable Long appointmentId,
            Authentication authentication) {

        // La pertenencia se comprueba antes de cancelar una reserva de este negocio.
        // El servicio exige además que la reserva pertenezca a ese mismo negocio.
        businessService.requireMembership(authenticatedEmail(authentication), businessId);

        Appointment appointment = appointmentService.cancel(businessId, appointmentId);

        // Devolvemos la reserva actualizada para que el cliente conozca su nuevo
        // estado.
        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);

        return ResponseEntity.ok(response);
    }

    // Confirma una reserva pendiente y devuelve su estado actualizado.
    @PatchMapping("/{appointmentId}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(
            @PathVariable Long businessId,
            @PathVariable Long appointmentId,
            Authentication authentication) {

        // La pertenencia se comprueba antes de confirmar una reserva de este negocio.
        // El servicio exige además que la reserva pertenezca a ese mismo negocio.
        businessService.requireMembership(authenticatedEmail(authentication), businessId);

        // Delegamos las reglas de confirmación al servicio.
        Appointment appointment = appointmentService.confirm(businessId, appointmentId);

        // Convertimos la entidad en un DTO para no exponerla directamente por la API.
        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);

        // HTTP 200 indica que la confirmación se ha completado correctamente.
        return ResponseEntity.ok(response);
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


package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.dto.AppointmentRequest;
import com.laura.chatbot_ia.dto.AppointmentResponse;
import com.laura.chatbot_ia.entity.Appointment;
import com.laura.chatbot_ia.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Expone el endpoint REST para crear reservas y delega la lógica al servicio.
@RestController
@RequestMapping("/api/businesses/{businessId}/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // Spring inyecta el servicio que valida y guarda las reservas.
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Valida el JSON recibido y devuelve HTTP 201 cuando la reserva se crea.
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @PathVariable Long businessId,
            @Valid @RequestBody AppointmentRequest request) {

        Appointment appointment = appointmentService.create(businessId, request);

        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Devuelve las reservas del negocio indicado con HTTP 200.
    @GetMapping
    public ResponseEntity<java.util.List<AppointmentResponse>> findByBusinessId(
            @PathVariable Long businessId) {

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
            @PathVariable Long appointmentId) {

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
            @PathVariable Long appointmentId) {

        // Delegamos las reglas de confirmación al servicio.
        Appointment appointment = appointmentService.confirm(businessId, appointmentId);

        // Convertimos la entidad en un DTO para no exponerla directamente por la API.
        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);

        // HTTP 200 indica que la confirmación se ha completado correctamente.
        return ResponseEntity.ok(response);
    }

}

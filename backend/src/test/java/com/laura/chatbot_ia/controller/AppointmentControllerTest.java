package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.dto.AppointmentRequest;
import com.laura.chatbot_ia.entity.Appointment;
import com.laura.chatbot_ia.entity.AppointmentStatus;
import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.exception.BusinessAccessDeniedException;
import com.laura.chatbot_ia.service.AppointmentService;
import com.laura.chatbot_ia.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Probamos las respuestas HTTP del controlador aislándolo del servicio de reservas.
@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private BusinessService businessService;

    private static final String AUTHENTICATED_EMAIL = "ana@clinica.es";

    private static final String APPOINTMENT_JSON = """
            {
              "offeredServiceId": 2,
              "customerName": "Cliente de prueba",
              "customerEmail": "cliente@example.com",
              "customerPhone": "600123456",
              "startAt": "2030-01-15T10:00:00"
            }
            """;

    // Comprobamos que un miembro lista las reservas de su negocio.
    @Test
    void findByBusinessIdReturnsOkWhenUserIsMember() throws Exception {
        Long businessId = 1L;
        Appointment appointment = appointment(AppointmentStatus.PENDING);

        when(appointmentService.findByBusinessId(businessId))
                .thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/businesses/{businessId}/appointments", businessId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Cliente de prueba"))
                .andExpect(jsonPath("$[0].serviceName").value("Consulta inicial"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // La pertenencia se comprueba antes de leer las reservas.
        InOrder order = inOrder(businessService, appointmentService);
        order.verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        order.verify(appointmentService).findByBusinessId(businessId);
    }

    // Comprobamos que listar reservas de otro negocio devuelve HTTP 403 sin leerlas.
    @Test
    void findByBusinessIdReturnsForbiddenWhenUserIsNotMember() throws Exception {
        Long businessId = 2L;
        denyMembership(businessId);

        mockMvc.perform(get("/api/businesses/{businessId}/appointments", businessId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/appointments"));

        verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        verifyNoInteractions(appointmentService);
    }

    // Comprobamos que un miembro crea una reserva en su negocio.
    @Test
    void createReturnsCreatedWhenUserIsMember() throws Exception {
        Long businessId = 1L;
        Appointment appointment = appointment(AppointmentStatus.PENDING);

        when(appointmentService.create(eq(businessId), any(AppointmentRequest.class)))
                .thenReturn(appointment);

        mockMvc.perform(post("/api/businesses/{businessId}/appointments", businessId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPOINTMENT_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Cliente de prueba"))
                .andExpect(jsonPath("$.serviceName").value("Consulta inicial"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // La pertenencia se comprueba antes de crear la reserva.
        InOrder order = inOrder(businessService, appointmentService);
        order.verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        order.verify(appointmentService).create(eq(businessId), any(AppointmentRequest.class));
    }

    // Comprobamos que crear una reserva en otro negocio devuelve HTTP 403 sin guardarla.
    @Test
    void createReturnsForbiddenWhenUserIsNotMember() throws Exception {
        Long businessId = 2L;
        denyMembership(businessId);

        mockMvc.perform(post("/api/businesses/{businessId}/appointments", businessId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPOINTMENT_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/appointments"));

        verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        verifyNoInteractions(appointmentService);
    }

    // Comprobamos que un miembro cancela una reserva de su negocio.
    @Test
    void cancelReturnsOkWhenUserIsMember() throws Exception {
        Long businessId = 1L;
        Long appointmentId = 10L;
        Appointment appointment = appointment(AppointmentStatus.CANCELLED);

        when(appointmentService.cancel(businessId, appointmentId)).thenReturn(appointment);

        mockMvc.perform(patch(
                        "/api/businesses/{businessId}/appointments/{appointmentId}/cancel",
                        businessId,
                        appointmentId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Cliente de prueba"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // La pertenencia se comprueba antes de cancelar la reserva.
        InOrder order = inOrder(businessService, appointmentService);
        order.verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        order.verify(appointmentService).cancel(businessId, appointmentId);
    }

    // Comprobamos que cancelar una reserva de otro negocio devuelve HTTP 403 sin modificarla.
    @Test
    void cancelReturnsForbiddenWhenUserIsNotMember() throws Exception {
        Long businessId = 2L;
        Long appointmentId = 10L;
        denyMembership(businessId);

        mockMvc.perform(patch(
                        "/api/businesses/{businessId}/appointments/{appointmentId}/cancel",
                        businessId,
                        appointmentId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/appointments/10/cancel"));

        verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        verifyNoInteractions(appointmentService);
    }

    // Comprobamos que un miembro confirma una reserva pendiente de su negocio.
    @Test
    void confirmReturnsOkWhenUserIsMember() throws Exception {
        Long businessId = 1L;
        Long appointmentId = 10L;
        Appointment appointment = appointment(AppointmentStatus.CONFIRMED);

        when(appointmentService.confirm(businessId, appointmentId)).thenReturn(appointment);

        mockMvc.perform(patch(
                        "/api/businesses/{businessId}/appointments/{appointmentId}/confirm",
                        businessId,
                        appointmentId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Cliente de prueba"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // La pertenencia se comprueba antes de confirmar la reserva.
        InOrder order = inOrder(businessService, appointmentService);
        order.verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        order.verify(appointmentService).confirm(businessId, appointmentId);
    }

    // Comprobamos que confirmar una reserva de otro negocio devuelve HTTP 403 sin modificarla.
    @Test
    void confirmReturnsForbiddenWhenUserIsNotMember() throws Exception {
        Long businessId = 2L;
        Long appointmentId = 10L;
        denyMembership(businessId);

        mockMvc.perform(patch(
                        "/api/businesses/{businessId}/appointments/{appointmentId}/confirm",
                        businessId,
                        appointmentId)
                        .with(authenticatedUser(AUTHENTICATED_EMAIL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/appointments/10/confirm"));

        verify(businessService).requireMembership(AUTHENTICATED_EMAIL, businessId);
        verifyNoInteractions(appointmentService);
    }

    // Simula que el usuario autenticado no pertenece al negocio de la ruta.
    private void denyMembership(Long businessId) {
        when(businessService.requireMembership(AUTHENTICATED_EMAIL, businessId))
                .thenThrow(new BusinessAccessDeniedException("No perteneces a ese negocio"));
    }

    // Deja en la petición el mismo principal que el filtro JWT guarda al validar el token.
    private RequestPostProcessor authenticatedUser(String email) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of());
        return request -> {
            request.setUserPrincipal(authentication);
            return request;
        };
    }

    // Construye una reserva con el negocio y el servicio que el DTO necesita leer.
    private Appointment appointment(AppointmentStatus status) {
        Business business = new Business();
        business.setName("Clínica Demo");

        OfferedService offeredService = new OfferedService();
        offeredService.setName("Consulta inicial");

        Appointment appointment = new Appointment();
        appointment.setBusiness(business);
        appointment.setOfferedService(offeredService);
        appointment.setCustomerName("Cliente de prueba");
        appointment.setCustomerEmail("cliente@example.com");
        appointment.setCustomerPhone("600123456");
        appointment.setStartAt(LocalDateTime.of(2030, 1, 15, 10, 0));
        appointment.setEndAt(LocalDateTime.of(2030, 1, 15, 10, 30));
        appointment.setPriceAtBooking(new BigDecimal("35.00"));
        appointment.setStatus(status);
        return appointment;
    }
}

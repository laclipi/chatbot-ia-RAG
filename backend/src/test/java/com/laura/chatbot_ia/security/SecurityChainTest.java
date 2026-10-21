// Comprueba la cadena real de Spring Security, no solo el principal del controlador.
package com.laura.chatbot_ia.security;

import com.laura.chatbot_ia.controller.AppointmentController;
import com.laura.chatbot_ia.controller.BusinessController;
import com.laura.chatbot_ia.controller.ChatController;
import com.laura.chatbot_ia.controller.HealthController;
import com.laura.chatbot_ia.controller.OfferedServiceController;
import com.laura.chatbot_ia.exception.BusinessAccessDeniedException;
import com.laura.chatbot_ia.service.AppointmentService;
import com.laura.chatbot_ia.service.BusinessService;
import com.laura.chatbot_ia.service.ChatService;
import com.laura.chatbot_ia.service.OfferedServiceManager;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Carga los controladores y la configuración de seguridad que usa la aplicación.
@WebMvcTest(controllers = {
        ChatController.class,
        BusinessController.class,
        OfferedServiceController.class,
        AppointmentController.class,
        HealthController.class
})
// El slice MVC no crea HttpSecurity. Estas autoconfiguraciones activan la cadena real.
@Import(SecurityConfig.class)
@ImportAutoConfiguration({
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
class SecurityChainTest {

    private static final String EMAIL = "visitante@correo.es";
    private static final String TOKEN = "token-de-prueba";
    private static final Long OTHER_BUSINESS_ID = 2L;

    private static final String SERVICE_JSON = """
            {
              "name": "Consulta",
              "price": 10,
              "durationMinutes": 30
            }
            """;

    private static final String APPOINTMENT_JSON = """
            {
              "offeredServiceId": 2,
              "customerName": "Cliente de prueba",
              "customerEmail": "cliente@example.com",
              "customerPhone": "600123456",
              "startAt": "2030-01-15T10:00:00"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private BusinessService businessService;

    @MockitoBean
    private OfferedServiceManager offeredServiceManager;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtService jwtService;

    // El cliente anónimo pregunta al negocio y recibe la respuesta del chat.
    @Test
    void publicChatRespondsWithoutToken() throws Exception {
        when(chatService.answer(1L, "¿Qué servicios ofrecéis?"))
                .thenReturn("Consulta general");

        mockMvc.perform(post("/api/businesses/1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿Qué servicios ofrecéis?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Consulta general"));

        verify(chatService).answer(1L, "¿Qué servicios ofrecéis?");
        verifyNoInteractions(businessService, offeredServiceManager, appointmentService);
    }

    // Un token válido no convierte al cliente en miembro: el chat sigue abierto.
    @Test
    void publicChatAcceptsTokenWithoutMembership() throws Exception {
        givenValidToken();
        when(chatService.answer(1L, "¿Qué servicios ofrecéis?"))
                .thenReturn("Consulta general");

        mockMvc.perform(post("/api/businesses/1/chat")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"¿Qué servicios ofrecéis?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Consulta general"));

        verifyNoInteractions(businessService);
    }

    // Salud, negocios, servicios y reservas no atienden una petición sin token.
    // El filtro responde 403 antes de llegar a los controladores.
    @Test
    void protectedRoutesRejectRequestsWithoutToken() throws Exception {
        rejectWithoutToken(get("/api/health"));
        rejectWithoutToken(get("/api/businesses"));
        rejectWithoutToken(post("/api/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Clínica Demo\"}"));
        rejectWithoutToken(get("/api/businesses/1"));
        rejectWithoutToken(get("/api/businesses/1/services"));
        rejectWithoutToken(get("/api/businesses/1/services/active"));
        rejectWithoutToken(get("/api/businesses/1/services/1"));
        rejectWithoutToken(post("/api/businesses/1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SERVICE_JSON));
        rejectWithoutToken(put("/api/businesses/1/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SERVICE_JSON));
        rejectWithoutToken(delete("/api/businesses/1/services/1"));
        rejectWithoutToken(patch("/api/businesses/1/services/1/active")
                .param("active", "false"));
        rejectWithoutToken(get("/api/businesses/1/appointments"));
        rejectWithoutToken(post("/api/businesses/1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(APPOINTMENT_JSON));
        rejectWithoutToken(patch("/api/businesses/1/appointments/1/cancel"));
        rejectWithoutToken(patch("/api/businesses/1/appointments/1/confirm"));
        // Solo el POST del chat es público; consultarlo con GET sigue cerrado.
        rejectWithoutToken(get("/api/businesses/1/chat"));

        verifyNoInteractions(
                businessService,
                offeredServiceManager,
                appointmentService,
                chatService);
    }

    // Consultar otro negocio exige pertenencia, aunque el token sea válido.
    @Test
    void authenticatedNonMemberCannotReadAnotherBusiness() throws Exception {
        givenValidToken();
        denyForeignBusiness();

        mockMvc.perform(get("/api/businesses/{id}", OTHER_BUSINESS_ID)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2"));

        verify(businessService).findById(OTHER_BUSINESS_ID, EMAIL);
        verifyNoInteractions(offeredServiceManager, appointmentService, chatService);
    }

    // El catálogo administrativo de otro negocio no se lee sin pertenencia.
    @Test
    void authenticatedNonMemberCannotListServices() throws Exception {
        givenValidToken();
        denyForeignBusiness();

        mockMvc.perform(get("/api/businesses/{businessId}/services", OTHER_BUSINESS_ID)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/services"));

        verify(businessService).requireMembership(EMAIL, OTHER_BUSINESS_ID);
        verifyNoInteractions(offeredServiceManager);
    }

    // Crear un servicio en otro negocio también queda bloqueado.
    @Test
    void authenticatedNonMemberCannotCreateService() throws Exception {
        givenValidToken();
        denyForeignBusiness();

        mockMvc.perform(post("/api/businesses/{businessId}/services", OTHER_BUSINESS_ID)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICE_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/services"));

        verify(businessService).requireMembership(EMAIL, OTHER_BUSINESS_ID);
        verifyNoInteractions(offeredServiceManager);
    }

    // Las reservas de otro negocio no se listan sin pertenencia.
    @Test
    void authenticatedNonMemberCannotListAppointments() throws Exception {
        givenValidToken();
        denyForeignBusiness();

        mockMvc.perform(get("/api/businesses/{businessId}/appointments", OTHER_BUSINESS_ID)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/appointments"));

        verify(businessService).requireMembership(EMAIL, OTHER_BUSINESS_ID);
        verifyNoInteractions(appointmentService);
    }

    // Crear una reserva en otro negocio también queda bloqueado.
    @Test
    void authenticatedNonMemberCannotCreateAppointment() throws Exception {
        givenValidToken();
        denyForeignBusiness();

        mockMvc.perform(post("/api/businesses/{businessId}/appointments", OTHER_BUSINESS_ID)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPOINTMENT_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No perteneces a ese negocio"))
                .andExpect(jsonPath("$.path").value("/api/businesses/2/appointments"));

        verify(businessService).requireMembership(EMAIL, OTHER_BUSINESS_ID);
        verifyNoInteractions(appointmentService);
    }

    // El filtro JWT solo autentica si el token firmado identifica a un usuario.
    private void givenValidToken() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(EMAIL);
        when(jwtService.extractClaims(TOKEN)).thenReturn(claims);
        when(jwtService.isTokenValid(TOKEN, EMAIL)).thenReturn(true);
    }

    // La pertenencia simulada falla antes de leer o modificar el otro negocio.
    private void denyForeignBusiness() {
        when(businessService.findById(eq(OTHER_BUSINESS_ID), eq(EMAIL)))
                .thenThrow(new BusinessAccessDeniedException("No perteneces a ese negocio"));
        when(businessService.requireMembership(EMAIL, OTHER_BUSINESS_ID))
                .thenThrow(new BusinessAccessDeniedException("No perteneces a ese negocio"));
    }

    // Sin token, la cadena corta la petición con 403 y el controlador no se ejecuta.
    // El 403 de pertenencia es otro: lleva el JSON "No perteneces a ese negocio".
    private void rejectWithoutToken(RequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("No perteneces a ese negocio"))))
                .andExpect(content().string(not(containsString("Chatbot IA API is running"))));
    }
}

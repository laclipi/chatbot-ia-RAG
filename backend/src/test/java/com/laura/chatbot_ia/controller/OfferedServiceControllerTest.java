// Probamos las respuestas HTTP del controlador aislándolo del gestor de servicios.
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.service.OfferedServiceManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Permiten simular una excepción del gestor y comprobar la respuesta HTTP.
import com.laura.chatbot_ia.exception.OfferedServiceNotFoundException;

// Permiten construir el JSON de entrada y simular la respuesta del gestor.
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import org.springframework.http.MediaType;

// Permite construir peticiones HTTP PUT para probar la actualización.
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

// Cargamos únicamente el controlador y simulamos su dependencia.
@WebMvcTest(OfferedServiceController.class)
class OfferedServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OfferedServiceManager offeredServiceManager;

    // Comprobamos que el endpoint de listado devuelve HTTP 200 y un array JSON.
    @Test
    void findByBusinessIdReturnsOkAndJsonArray() throws Exception {
        Long businessId = 1L;

        when(offeredServiceManager.findByBusinessId(businessId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/businesses/{businessId}/services", businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(offeredServiceManager).findByBusinessId(businessId);
    }

    // Comprobamos que crear un servicio devuelve HTTP 201 y los datos en JSON.
    @Test
    void createReturnsCreatedAndJsonService() throws Exception {
        Long businessId = 1L;

        // Simulamos el servicio creado que devolvería el gestor.
        OfferedService createdService = new OfferedService();
        createdService.setName("Consulta inicial");
        createdService.setDescription("Primera consulta");
        createdService.setPrice(new java.math.BigDecimal("35.00"));
        createdService.setDurationMinutes(30);
        createdService.setActive(true);

        when(offeredServiceManager.create(eq(businessId), any(OfferedService.class)))
                .thenReturn(createdService);

        // Enviamos una petición POST con los campos obligatorios del servicio.
        mockMvc.perform(post("/api/businesses/{businessId}/services", businessId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Consulta inicial",
                          "description": "Primera consulta",
                          "price": 35.00,
                          "durationMinutes": 30,
                          "active": true
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Consulta inicial"))
                .andExpect(jsonPath("$.price").value(35.00))
                .andExpect(jsonPath("$.durationMinutes").value(30));

        // Verificamos que el controlador delega la creación al gestor.
        verify(offeredServiceManager).create(eq(businessId), any(OfferedService.class));
    }

    // Verificamos que un servicio inexistente se traduce en HTTP 404 y un ApiError.
    @Test
    void findByIdAndBusinessIdReturnsNotFoundWhenServiceDoesNotExist() throws Exception {
        Long businessId = 1L;
        Long serviceId = 99L;

        // Simulamos el error que lanzaría el gestor al no encontrar el servicio.
        when(offeredServiceManager.findByIdAndBusinessId(serviceId, businessId))
                .thenThrow(new OfferedServiceNotFoundException(serviceId, businessId));

        // Solicitamos el servicio y comprobamos el estado y los datos del error.
        mockMvc.perform(get(
                "/api/businesses/{businessId}/services/id/{serviceId}",
                businessId,
                serviceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("No existe un servicio con ID 99 para el negocio con ID 1"))
                .andExpect(jsonPath("$.path")
                        .value("/api/businesses/1/services/id/99"));
    }

    // Comprobamos que enviar un servicio sin nombre devuelve HTTP 400.
    @Test
    void createReturnsBadRequestWhenNameIsBlank() throws Exception {

        // Enviamos un JSON que incumple la validación @NotBlank del nombre.
        mockMvc.perform(post("/api/businesses/{businessId}/services", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": " ",
                          "description": "Primera consulta",
                          "price": 35.00,
                          "durationMinutes": 30,
                          "active": true
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El nombre del servicio es obligatorio"));
    }

    // Comprobamos que actualizar un servicio devuelve HTTP 200 y los datos nuevos.
    @Test
    void updateReturnsOkAndUpdatedService() throws Exception {
        Long businessId = 1L;
        Long serviceId = 10L;

        // Simulamos el servicio que devuelve el gestor tras actualizarlo.
        OfferedService updatedService = new OfferedService();
        updatedService.setName("Consulta avanzada");
        updatedService.setDescription("Consulta de seguimiento");
        updatedService.setPrice(new java.math.BigDecimal("50.00"));
        updatedService.setDurationMinutes(45);
        updatedService.setActive(true);

        when(offeredServiceManager.update(
                eq(businessId),
                eq(serviceId),
                any(OfferedService.class)))
                .thenReturn(updatedService);

        // Enviamos un PUT con los nuevos datos del servicio.
        mockMvc.perform(put(
                "/api/businesses/{businessId}/services/{serviceId}",
                businessId,
                serviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Consulta avanzada",
                          "description": "Consulta de seguimiento",
                          "price": 50.00,
                          "durationMinutes": 45,
                          "active": true
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consulta avanzada"))
                .andExpect(jsonPath("$.description").value("Consulta de seguimiento"))
                .andExpect(jsonPath("$.price").value(50.00))
                .andExpect(jsonPath("$.durationMinutes").value(45));

        // Verificamos que el controlador delega la actualización al gestor.
        verify(offeredServiceManager).update(
                eq(businessId),
                eq(serviceId),
                any(OfferedService.class));
    }

}

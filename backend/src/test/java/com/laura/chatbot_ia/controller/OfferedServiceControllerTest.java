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

// Imports necesarios para la petición DELETE y su respuesta sin cuerpo.
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

// Permiten simular una excepción del gestor y comprobar la respuesta HTTP.
import com.laura.chatbot_ia.exception.OfferedServiceNotFoundException;

// Permiten construir el JSON de entrada y simular la respuesta del gestor.
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import org.springframework.http.MediaType;

// Permite construir peticiones HTTP PATCH para cambiar el estado del servicio.
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

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

    // Comprobamos que eliminar un servicio devuelve HTTP 204 sin cuerpo de
    // respuesta.
    @Test
    void deleteReturnsNoContent() throws Exception {
        Long businessId = 1L;
        Long serviceId = 10L;

        // Simulamos que el gestor elimina el servicio sin lanzar ninguna excepción.
        org.mockito.Mockito.doNothing()
                .when(offeredServiceManager)
                .delete(businessId, serviceId);

        // Enviamos la petición DELETE y verificamos que la respuesta no tiene
        // contenido.
        mockMvc.perform(delete(
                "/api/businesses/{businessId}/services/{serviceId}",
                businessId,
                serviceId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verificamos que el controlador delega la eliminación al gestor.
        verify(offeredServiceManager).delete(businessId, serviceId);
    }

    // Comprobamos que desactivar un servicio devuelve HTTP 200 y active=false.
    @Test
    void changeActiveStatusReturnsOkAndInactiveService() throws Exception {
        Long businessId = 1L;
        Long serviceId = 10L;

        // Simulamos el servicio devuelto por el gestor tras desactivarlo.
        OfferedService inactiveService = new OfferedService();
        inactiveService.setName("Consulta inicial");
        inactiveService.setDescription("Primera consulta");
        inactiveService.setPrice(new java.math.BigDecimal("35.00"));
        inactiveService.setDurationMinutes(30);
        inactiveService.setActive(false);

        when(offeredServiceManager.changeActiveStatus(businessId, serviceId, false))
                .thenReturn(inactiveService);

        // Enviamos PATCH con el parámetro active=false.
        mockMvc.perform(patch(
                "/api/businesses/{businessId}/services/{serviceId}/active",
                businessId,
                serviceId)
                .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consulta inicial"))
                .andExpect(jsonPath("$.active").value(false));

        // Verificamos que el controlador delega el cambio de estado al gestor.
        verify(offeredServiceManager)
                .changeActiveStatus(businessId, serviceId, false);
    }

    // Comprobamos que el endpoint de servicios activos devuelve HTTP 200 y una
    // lista JSON.
    @Test
    void findActiveByBusinessIdReturnsOkAndJsonArray() throws Exception {
        Long businessId = 1L;

        // Simulamos que el gestor devuelve un servicio activo.
        OfferedService activeService = new OfferedService();
        activeService.setName("Consulta inicial");
        activeService.setDescription("Primera consulta");
        activeService.setPrice(new java.math.BigDecimal("35.00"));
        activeService.setDurationMinutes(30);
        activeService.setActive(true);

        when(offeredServiceManager.findByBusinessId(businessId))
                .thenReturn(List.of(activeService));

        // Consultamos el endpoint y verificamos que devuelve una lista con un servicio
        // activo.
        mockMvc.perform(get(
                "/api/businesses/{businessId}/services/active",
                businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Consulta inicial"))
                .andExpect(jsonPath("$[0].active").value(true));

        // Verificamos que el controlador delega la consulta al gestor.
        verify(offeredServiceManager).findByBusinessId(businessId);
    }

}

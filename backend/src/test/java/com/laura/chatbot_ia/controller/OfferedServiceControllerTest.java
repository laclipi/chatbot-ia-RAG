package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.exception.OfferedServiceNotFoundException;
import com.laura.chatbot_ia.service.OfferedServiceManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Probamos las respuestas HTTP del controlador aislándolo del gestor de servicios.
@WebMvcTest(OfferedServiceController.class)
class OfferedServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OfferedServiceManager offeredServiceManager;

    // Comprobamos que el listado devuelve HTTP 200 y un array JSON vacío.
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

    // Comprobamos que crear un servicio devuelve HTTP 201 y los datos del DTO de
    // respuesta.
    @Test
    void createReturnsCreatedAndJsonService() throws Exception {
        Long businessId = 1L;

        // Simulamos la entidad creada que devolvería el gestor.
        OfferedService createdService = new OfferedService();
        createdService.setName("Consulta inicial");
        createdService.setDescription("Primera consulta");
        createdService.setPrice(new BigDecimal("35.00"));
        createdService.setDurationMinutes(30);
        createdService.setActive(true);

        when(offeredServiceManager.create(eq(businessId), any(OfferedService.class)))
                .thenReturn(createdService);

        // Enviamos solo los campos admitidos por OfferedServiceRequest.
        mockMvc.perform(post("/api/businesses/{businessId}/services", businessId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Consulta inicial",
                          "description": "Primera consulta",
                          "price": 35.00,
                          "durationMinutes": 30
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.name").value("Consulta inicial"))
                .andExpect(jsonPath("$.description").value("Primera consulta"))
                .andExpect(jsonPath("$.price").value(35.00))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.active").value(true));

        // Verificamos que el controlador delega la creación al gestor.
        verify(offeredServiceManager).create(eq(businessId), any(OfferedService.class));
    }

    // Verificamos que un servicio inexistente devuelve HTTP 404 y un ApiError.
    @Test
    void findByIdAndBusinessIdReturnsNotFoundWhenServiceDoesNotExist() throws Exception {
        Long businessId = 1L;
        Long serviceId = 99L;

        // Simulamos el error que lanzaría el gestor al no encontrar el servicio.
        when(offeredServiceManager.findByIdAndBusinessId(serviceId, businessId))
                .thenThrow(new OfferedServiceNotFoundException(serviceId, businessId));

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

    // Comprobamos que un nombre en blanco devuelve HTTP 400 por la validación del
    // DTO.
    @Test
    void createReturnsBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/businesses/{businessId}/services", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": " ",
                          "description": "Primera consulta",
                          "price": 35.00,
                          "durationMinutes": 30
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El nombre del servicio es obligatorio"));
    }

    // Comprobamos que un precio negativo devuelve HTTP 400 por la validación del
    // DTO.
    @Test
    void createReturnsBadRequestWhenPriceIsNegative() throws Exception {
        mockMvc.perform(post("/api/businesses/{businessId}/services", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Consulta inicial",
                          "description": "Primera consulta",
                          "price": -5.00,
                          "durationMinutes": 30
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El precio no puede ser negativo"));
    }

    // Comprobamos que una duración de cero devuelve HTTP 400 por la validación del
    // DTO.
    @Test
    void createReturnsBadRequestWhenDurationIsZero() throws Exception {
        mockMvc.perform(post("/api/businesses/{businessId}/services", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Consulta inicial",
                          "description": "Primera consulta",
                          "price": 35.00,
                          "durationMinutes": 0
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("La duración debe ser de al menos 1 minuto"));
    }

    // Comprobamos que actualizar con un nombre en blanco devuelve HTTP 400.
    @Test
    void updateReturnsBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(put(
                "/api/businesses/{businessId}/services/{serviceId}",
                1L,
                10L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": " ",
                          "description": "Primera consulta",
                          "price": 35.00,
                          "durationMinutes": 30
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El nombre del servicio es obligatorio"));
    }

    // Comprobamos que actualizar devuelve HTTP 200 y los datos nuevos del DTO.
    @Test
    void updateReturnsOkAndUpdatedService() throws Exception {
        Long businessId = 1L;
        Long serviceId = 10L;

        // Simulamos la entidad que devuelve el gestor tras la actualización.
        OfferedService updatedService = new OfferedService();

        updatedService.setName("Consulta avanzada");
        updatedService.setDescription("Consulta de seguimiento");
        updatedService.setPrice(new BigDecimal("50.00"));
        updatedService.setDurationMinutes(45);
        updatedService.setActive(true);

        when(offeredServiceManager.update(
                eq(businessId),
                eq(serviceId),
                any(OfferedService.class)))
                .thenReturn(updatedService);

        // Enviamos los campos permitidos por OfferedServiceRequest, sin el estado
        // activo.
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
                          "durationMinutes": 45
                        }
                        """))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name").value("Consulta avanzada"))
                .andExpect(jsonPath("$.description").value("Consulta de seguimiento"))
                .andExpect(jsonPath("$.price").value(50.00))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.active").value(true));

        // Verificamos que el controlador delega la actualización al gestor.
        verify(offeredServiceManager).update(
                eq(businessId),
                eq(serviceId),
                any(OfferedService.class));
    }

    // Comprobamos que eliminar un servicio devuelve HTTP 204 sin cuerpo.
    @Test
    void deleteReturnsNoContent() throws Exception {
        Long businessId = 1L;
        Long serviceId = 10L;

        // Simulamos la eliminación sin errores.
        doNothing().when(offeredServiceManager).delete(businessId, serviceId);

        mockMvc.perform(delete(
                "/api/businesses/{businessId}/services/{serviceId}",
                businessId,
                serviceId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(offeredServiceManager).delete(businessId, serviceId);
    }

    // Comprobamos que desactivar un servicio devuelve HTTP 200 y active=false.
    @Test
    void changeActiveStatusReturnsOkAndInactiveService() throws Exception {
        Long businessId = 1L;
        Long serviceId = 10L;

        // Simulamos la entidad devuelta por el gestor tras desactivar el servicio.
        OfferedService inactiveService = new OfferedService();

        inactiveService.setName("Consulta inicial");
        inactiveService.setDescription("Primera consulta");
        inactiveService.setPrice(new BigDecimal("35.00"));
        inactiveService.setDurationMinutes(30);
        inactiveService.setActive(false);

        when(offeredServiceManager.changeActiveStatus(businessId, serviceId, false))
                .thenReturn(inactiveService);

        mockMvc.perform(patch(
                "/api/businesses/{businessId}/services/{serviceId}/active",
                businessId,
                serviceId)
                .param("active", "false"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name").value("Consulta inicial"))
                .andExpect(jsonPath("$.active").value(false));

        verify(offeredServiceManager)
                .changeActiveStatus(businessId, serviceId, false);
    }

    // Comprobamos que el endpoint de servicios activos devuelve una lista JSON.
    @Test
    void findActiveByBusinessIdReturnsOkAndJsonArray() throws Exception {
        Long businessId = 1L;

        // Simulamos que el gestor devuelve un servicio activo.
        OfferedService activeService = new OfferedService();

        activeService.setName("Consulta inicial");
        activeService.setDescription("Primera consulta");
        activeService.setPrice(new BigDecimal("35.00"));
        activeService.setDurationMinutes(30);
        activeService.setActive(true);

        when(offeredServiceManager.findByBusinessId(businessId))
                .thenReturn(List.of(activeService));

        mockMvc.perform(get(
                "/api/businesses/{businessId}/services/active",
                businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))

                .andExpect(jsonPath("$[0].name").value("Consulta inicial"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(offeredServiceManager).findByBusinessId(businessId);
    }

    // Comprobamos que consultar un servicio devuelve HTTP 200 y sus datos mediante
    // DTO.
    @Test
    void findByIdAndBusinessIdReturnsOkAndService() throws Exception {
        Long businessId = 1L;
        Long serviceId = 10L;

        // Simulamos la entidad que devuelve el gestor para esos identificadores.
        OfferedService service = new OfferedService();

        service.setName("Consulta inicial");
        service.setDescription("Primera consulta");
        service.setPrice(new BigDecimal("35.00"));
        service.setDurationMinutes(30);
        service.setActive(true);

        when(offeredServiceManager.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(service);

        mockMvc.perform(get(
                "/api/businesses/{businessId}/services/id/{serviceId}",
                businessId,
                serviceId))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name").value("Consulta inicial"))
                .andExpect(jsonPath("$.description").value("Primera consulta"))
                .andExpect(jsonPath("$.price").value(35.00))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.active").value(true));

        verify(offeredServiceManager).findByIdAndBusinessId(serviceId, businessId);
    }
}

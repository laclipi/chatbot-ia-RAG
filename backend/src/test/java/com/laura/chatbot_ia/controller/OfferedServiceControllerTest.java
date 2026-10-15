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
}

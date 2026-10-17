
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.dto.ChatRequest;
import com.laura.chatbot_ia.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Probamos el endpoint HTTP del chatbot sin levantar la base de datos.
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Simulamos la lógica del chatbot para probar solo el controlador.
    @MockitoBean
    private ChatService chatService;

    // Comprobamos que una pregunta válida devuelve la respuesta del servicio.
    @Test
    void chatReturnsAnswerForValidMessage() throws Exception {
        when(chatService.answer(eq(1L), anyString()))
                .thenReturn("Ofrecemos consultas generales.");

        ChatRequest request = new ChatRequest();
        request.setMessage("¿Qué servicios ofrecéis?");

        mockMvc.perform(post("/api/businesses/1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"¿Qué servicios ofrecéis?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer")
                        .value("Ofrecemos consultas generales."));

        // Verificamos que el controlador transmite el negocio y la pregunta correctos.
        verify(chatService).answer(1L, "¿Qué servicios ofrecéis?");
    }

    // Comprobamos que una pregunta vacía se rechaza por la validación del DTO.
    @Test
    void chatReturnsBadRequestWhenMessageIsBlank() throws Exception {
        mockMvc.perform(post("/api/businesses/1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }
}

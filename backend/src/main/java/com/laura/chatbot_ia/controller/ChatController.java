
// Controlador REST que expone el endpoint de conversación del chatbot.
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.dto.ChatRequest;
import com.laura.chatbot_ia.dto.ChatResponse;
import com.laura.chatbot_ia.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses/{businessId}/chat")
public class ChatController {

    private final ChatService chatService;

    // Spring inyecta el servicio que prepara las respuestas del chatbot.
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Recibe la pregunta del usuario y devuelve la respuesta del chatbot.
    @PostMapping
    public ChatResponse chat(
            @PathVariable Long businessId,
            @Valid @RequestBody ChatRequest request) {

        // Delegamos la consulta y envolvemos el resultado en el DTO de respuesta.
        String answer = chatService.answer(businessId, request.getMessage());

        return new ChatResponse(answer);
    }
}

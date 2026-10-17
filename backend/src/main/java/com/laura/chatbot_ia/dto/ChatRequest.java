
package com.laura.chatbot_ia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO de entrada: valida la pregunta que el usuario envía al chatbot.
public class ChatRequest {

    // Exigimos una pregunta con contenido y limitamos su longitud.
    @NotBlank(message = "La pregunta no puede estar vacía")
    @Size(max = 1000, message = "La pregunta no puede superar los 1000 caracteres")
    private String message;

    // Constructor vacío para que Spring pueda crear el DTO desde el JSON.
    public ChatRequest() {
    }

    // Devuelve el mensaje enviado por el usuario.
    public String getMessage() {
        return message;
    }

    // Guarda el mensaje recibido en la petición.
    public void setMessage(String message) {
        this.message = message;
    }
}

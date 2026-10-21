package com.laura.chatbot_ia.dto;

// Define los datos públicos que devuelve la API tras un login correcto.
public record LoginResponse(

        // Token firmado que el cliente enviará para autenticarse.
        String accessToken,

        // Indica el tipo de token que debe usar el cliente.
        String tokenType

) {
}

package com.laura.chatbot_ia.dto;

// Define los datos públicos que devuelve la API tras registrar una cuenta.
// Excluye la contraseña y su hash.
public record RegisterResponse(
        Long id,
        String email) {
}

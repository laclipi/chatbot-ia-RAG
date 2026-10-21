package com.laura.chatbot_ia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Define y valida las credenciales que recibe el endpoint de login.
public record LoginRequest(

        // Exige un email con formato válido.
        @NotBlank @Email String email,

        // Exige una contraseña y limita su longitud para evitar entradas excesivas.
        @NotBlank @Size(max = 72) String password

) {
}
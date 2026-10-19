
package com.laura.chatbot_ia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Define los datos permitidos al registrar una cuenta de gestión.
public record RegisterRequest(

        // Exige un email con formato válido y limita su longitud.
        @NotBlank @Email @Size(max = 150) String email,

        // Exige una contraseña de al menos 12 caracteres.
        @NotBlank @Size(min = 12, max = 72) String password

) {
}

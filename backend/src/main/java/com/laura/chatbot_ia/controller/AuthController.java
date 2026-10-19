
package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.dto.RegisterRequest;
import com.laura.chatbot_ia.dto.RegisterResponse;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Expone los endpoints HTTP relacionados con la autenticación.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    // Spring inyecta el servicio que contiene la lógica de registro.
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Registra un usuario y devuelve solo sus datos públicos.
    // Un email repetido sale como EmailAlreadyRegisteredException y el
    // manejador global responde 409 sin reenviar la petición a /error.
    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.register(request);

        // Nunca devolvemos la contraseña ni su hash.
        return new RegisterResponse(user.getId(), user.getEmail());
    }
}


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
// DTOs para recibir credenciales y devolver el token.
import com.laura.chatbot_ia.dto.LoginRequest;
import com.laura.chatbot_ia.dto.LoginResponse;

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

    // Autentica al usuario y devuelve un JWT si las credenciales son correctas.
    // InvalidCredentialsException llega al manejador global, que responde 401
    // con ApiError. No usamos ResponseStatusException: sendError reenviaría
    // la petición a /error y Spring Security podría convertirla en 403.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        String token = userService.login(request);

        // Indica al cliente que el token es de tipo Bearer.
        return new LoginResponse(token, "Bearer");
    }
}


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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

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
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        try {
            // El servicio verifica la contraseña y genera el token.
            String token = userService.login(request);

            // Indica al cliente que el token es de tipo Bearer.
            return new LoginResponse(token, "Bearer");

        } catch (BadCredentialsException exception) {
            // Devuelve el mismo error para email inexistente y contraseña incorrecta.
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas");
        }
    }
}

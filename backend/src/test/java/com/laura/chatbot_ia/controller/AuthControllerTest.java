package com.laura.chatbot_ia.controller;

import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.EmailAlreadyRegisteredException;
import com.laura.chatbot_ia.exception.InvalidCredentialsException;
import com.laura.chatbot_ia.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Comprueba el contrato HTTP de registro y login, con el manejador global de errores.
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    private static final String REGISTER_JSON = """
            {
              "email": "ana@clinica.es",
              "password": "contrasena-segura"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // El alta responde los datos públicos y no incluye la contraseña ni su hash.
    @Test
    void registerReturnsPublicUser() throws Exception {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setEmail("ana@clinica.es");
        when(userService.register(any())).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("ana@clinica.es"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // Un email repetido conserva el 409 y el formato ApiError.
    @Test
    void registerReturnsConflictWhenEmailAlreadyExists() throws Exception {
        when(userService.register(any())).thenThrow(
                new EmailAlreadyRegisteredException("Ya existe una cuenta con ese email"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya existe una cuenta con ese email"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    // El login correcto sigue devolviendo el token y el tipo Bearer.
    @Test
    void loginReturnsBearerToken() throws Exception {
        when(userService.login(any())).thenReturn("token-de-prueba");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-de-prueba"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // Una contraseña incorrecta responde 401 con ApiError, no un 403 de Spring Security.
    @Test
    void loginReturnsUnauthorizedWhenPasswordIsWrong() throws Exception {
        when(userService.login(any())).thenThrow(
                new InvalidCredentialsException("Credenciales incorrectas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    // Un email sin cuenta produce el mismo 401, sin indicar que la cuenta no existe.
    @Test
    void loginReturnsUnauthorizedWhenUserDoesNotExist() throws Exception {
        when(userService.login(any())).thenThrow(
                new InvalidCredentialsException("Credenciales incorrectas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nadie@clinica.es",
                                  "password": "contrasena-segura"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }
}

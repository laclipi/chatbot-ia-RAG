package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.dto.LoginRequest;
import com.laura.chatbot_ia.dto.RegisterRequest;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.EmailAlreadyRegisteredException;
import com.laura.chatbot_ia.exception.InvalidCredentialsException;
import com.laura.chatbot_ia.repository.UserRepository;
import com.laura.chatbot_ia.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Comprueba el registro y el login sin PostgreSQL ni la cadena HTTP.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "ana@clinica.es";
    private static final String PASSWORD = "contrasena-segura";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;

    private UserService userService;

    // BCrypt real: el mock no serviría para demostrar que la contraseña queda cifrada.
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    // El alta normaliza el email y guarda un hash BCrypt, nunca la contraseña original.
    @Test
    void registerStoresNormalizedEmailAndBcryptHash() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(
                new RegisterRequest("  Ana@Clinica.es  ", PASSWORD));

        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getPasswordHash().startsWith("$2a$")
                || saved.getPasswordHash().startsWith("$2b$"));
        assertTrue(passwordEncoder.matches(PASSWORD, saved.getPasswordHash()));
        assertFalse(saved.getPasswordHash().contains(PASSWORD));
    }

    // Un email repetido no crea otra cuenta.
    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        EmailAlreadyRegisteredException exception = assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> userService.register(new RegisterRequest(EMAIL, PASSWORD)));

        assertEquals("Ya existe una cuenta con ese email", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // Una contraseña distinta del hash almacenado no genera token.
    @Test
    void loginRejectsWrongPassword() {
        User user = userWithHash(passwordEncoder.encode(PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(new LoginRequest(EMAIL, "otra-contrasena-distinta")));

        assertEquals("Credenciales incorrectas", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    // Un email sin cuenta recibe el mismo error que una contraseña incorrecta.
    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(new LoginRequest(EMAIL, PASSWORD)));

        assertEquals("Credenciales incorrectas", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    // Con el hash correcto, el token identifica al email ya normalizado.
    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        User user = userWithHash(passwordEncoder.encode(PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(EMAIL)).thenReturn("token-de-prueba");

        String token = userService.login(
                new LoginRequest("  Ana@Clinica.es  ", PASSWORD));

        assertEquals("token-de-prueba", token);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).findByEmail(emailCaptor.capture());
        assertEquals(EMAIL, emailCaptor.getValue());
    }

    private User userWithHash(String passwordHash) {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(passwordHash);
        return user;
    }
}

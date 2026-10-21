
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.dto.RegisterRequest;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.EmailAlreadyRegisteredException;
import com.laura.chatbot_ia.exception.InvalidCredentialsException;
import com.laura.chatbot_ia.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// Importa el DTO de entrada del login.
import com.laura.chatbot_ia.dto.LoginRequest;
// Permite generar el token de acceso.
import com.laura.chatbot_ia.security.JwtService;

// Contiene la lógica de negocio relacionada con las cuentas de usuario.
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // Servicio responsable de generar y validar tokens JWT.
    private final JwtService jwtService;

    // Spring inyecta las dependencias necesarias para registro y autenticación.
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Registra una cuenta y guarda únicamente el hash de su contraseña.
    @Transactional
    public User register(RegisterRequest request) {

        // Evita registrar un email que ya pertenezca a otro usuario.
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            // El manejador global traduce esta excepción a 409 con ResponseEntity.
            // Así la respuesta no usa sendError ni el reenvío a /error.
            throw new EmailAlreadyRegisteredException(
                    "Ya existe una cuenta con ese email");
        }

        // Construye el usuario sin almacenar nunca la contraseña original.
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password()));

        // Persiste la cuenta y devuelve la entidad guardada.
        return userRepository.save(user);
    }

    // Verifica las credenciales y genera un JWT si son correctas.
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {

        // Normaliza el email igual que en el registro.
        String email = request.email().trim().toLowerCase();

        // El mismo error cubre el email inexistente y la contraseña incorrecta.
        // Así la respuesta no revela cuál de las dos comprobaciones ha fallado.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Credenciales incorrectas"));

        // Compara la contraseña recibida con el hash BCrypt almacenado.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales incorrectas");
        }

        // Solo genera el token después de validar las credenciales.
        return jwtService.generateToken(user.getEmail());
    }
}

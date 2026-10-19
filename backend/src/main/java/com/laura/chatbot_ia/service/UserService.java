
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.dto.RegisterRequest;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.EmailAlreadyRegisteredException;
import com.laura.chatbot_ia.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Contiene la lógica de negocio relacionada con las cuentas de usuario.
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring inyecta el repositorio y el codificador de contraseñas.
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}

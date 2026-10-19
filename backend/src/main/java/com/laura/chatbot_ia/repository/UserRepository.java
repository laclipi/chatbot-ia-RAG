
package com.laura.chatbot_ia.repository;

import com.laura.chatbot_ia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Proporciona las operaciones de persistencia de los usuarios.
public interface UserRepository extends JpaRepository<User, Long> {

    // Busca un usuario por email; puede no existir si todavía no está registrado.
    Optional<User> findByEmail(String email);

    // Comprueba si el email ya pertenece a otro usuario registrado.
    boolean existsByEmail(String email);
}

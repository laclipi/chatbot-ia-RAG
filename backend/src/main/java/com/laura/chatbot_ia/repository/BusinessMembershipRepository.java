package com.laura.chatbot_ia.repository;

import com.laura.chatbot_ia.entity.BusinessMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Persistencia de las pertenencias entre usuarios y negocios.
public interface BusinessMembershipRepository extends JpaRepository<BusinessMembership, Long> {

    // Comprueba el par sin cargar el usuario, el negocio ni el hash de la
    // contraseña.
    // Spring Data resuelve user.id y business.id a partir del nombre del método.
    boolean existsByUserIdAndBusinessId(Long userId, Long businessId);

    // Lista las membresías de un usuario. Spring Data recorre user.id.
    List<BusinessMembership> findByUserId(Long userId);
}

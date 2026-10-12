
package com.laura.chatbot_ia.repository;

import com.laura.chatbot_ia.entity.OfferedService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferedServiceRepository
        extends JpaRepository<OfferedService, Long> {

    // Busca los servicios que pertenecen a un negocio concreto.
    List<OfferedService> findByBusinessId(Long businessId);

    // Busca un servicio solo si pertenece al negocio indicado.
    Optional<OfferedService> findByIdAndBusinessId(
            Long id,
            Long businessId);
}

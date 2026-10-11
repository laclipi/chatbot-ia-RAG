
package com.laura.chatbot_ia.repository;

import com.laura.chatbot_ia.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

// Gestiona las operaciones de acceso a datos para Business.
// JpaRepository recibe la entidad y el tipo de su identificador.
public interface BusinessRepository extends JpaRepository<Business, Long> {
}

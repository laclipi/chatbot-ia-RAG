
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.BusinessMembership;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.AuthenticatedUserNotFoundException;
import com.laura.chatbot_ia.repository.BusinessMembershipRepository;
import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Indica a Spring que esta clase contiene lógica de negocio.
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final BusinessMembershipRepository businessMembershipRepository;

    // Spring inyecta los repositorios que participan en la creación del negocio.
    public BusinessService(
            BusinessRepository businessRepository,
            UserRepository userRepository,
            BusinessMembershipRepository businessMembershipRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.businessMembershipRepository = businessMembershipRepository;
    }

    // Recupera todos los negocios registrados.
    public List<Business> findAll() {
        return businessRepository.findAll();
    }

    // Busca un negocio por su ID.
    // Optional representa que el negocio puede existir o no.
    public Optional<Business> findById(Long id) {
        return businessRepository.findById(id);
    }

    // Guarda un negocio nuevo o actualiza uno existente.
    public Business save(Business business) {
        return businessRepository.save(business);
    }

    // Crea el negocio y la pertenencia de su autor en la misma transacción.
    @Transactional
    public Business create(Business business, String ownerEmail) {

        // Misma normalización que el registro y el login.
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new AuthenticatedUserNotFoundException();
        }

        String email = ownerEmail.trim().toLowerCase();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(AuthenticatedUserNotFoundException::new);

        // El identificador generado queda disponible para la clave foránea.
        Business savedBusiness = businessRepository.save(business);

        BusinessMembership membership = new BusinessMembership();
        membership.setUser(owner);
        membership.setBusiness(savedBusiness);
        businessMembershipRepository.save(membership);

        return savedBusiness;
    }
}


package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.BusinessMembership;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.AuthenticatedUserNotFoundException;
import com.laura.chatbot_ia.exception.BusinessAccessDeniedException;
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

    // Devuelve solo los negocios vinculados al email autenticado.
    @Transactional(readOnly = true)
    public List<Business> findAll(String email) {
        User user = findAuthenticatedUser(email);

        return businessMembershipRepository.findByUserId(user.getId())
                .stream()
                .map(BusinessMembership::getBusiness)
                .toList();
    }

    // Comprueba la pertenencia antes de buscar el negocio.
    public Optional<Business> findById(Long id, String email) {
        requireMembership(email, id);
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

    // Comprueba que el email pertenece al negocio antes de leer o modificar sus datos.
    @Transactional(readOnly = true)
    public User requireMembership(String email, Long businessId) {
        User user = findAuthenticatedUser(email);

        boolean member = businessMembershipRepository
                .existsByUserIdAndBusinessId(user.getId(), businessId);

        if (!member) {
            throw new BusinessAccessDeniedException("No perteneces a ese negocio");
        }

        return user;
    }

    // Resuelve la cuenta del email con la misma normalización que el registro y el login.
    private User findAuthenticatedUser(String email) {
        if (email == null || email.isBlank()) {
            throw new AuthenticatedUserNotFoundException();
        }

        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(AuthenticatedUserNotFoundException::new);
    }
}

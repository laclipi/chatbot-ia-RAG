
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Indica a Spring que esta clase contiene lógica de negocio.
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    // Spring inyecta automáticamente el repositorio mediante el constructor.
    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
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
}

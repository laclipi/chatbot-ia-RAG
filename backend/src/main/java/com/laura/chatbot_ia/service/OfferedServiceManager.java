
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.OfferedServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import com.laura.chatbot_ia.exception.BusinessNotFoundException;

@Service
public class OfferedServiceManager {

    private final OfferedServiceRepository offeredServiceRepository;
    private final BusinessRepository businessRepository;

    public OfferedServiceManager(
            OfferedServiceRepository offeredServiceRepository,
            BusinessRepository businessRepository) {
        this.offeredServiceRepository = offeredServiceRepository;
        this.businessRepository = businessRepository;
    }

    // Devuelve los servicios de un negocio, si este existe.
    public List<OfferedService> findByBusinessId(Long businessId) {

        if (!businessRepository.existsById(businessId)) {
            throw new BusinessNotFoundException(businessId);
        }

        return offeredServiceRepository.findByBusinessId(businessId);
    }

    // Comprueba el negocio y asocia el servicio antes de guardarlo.
    public OfferedService create(Long businessId, OfferedService offeredService) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));

        offeredService.setBusiness(business);

        return offeredServiceRepository.save(offeredService);
    }
}


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

    // Busca un servicio por su ID y comprueba que pertenece al negocio indicado.
    public OfferedService findByIdAndBusinessId(Long serviceId, Long businessId) {

        // Comprobamos primero que el negocio exista.
        if (!businessRepository.existsById(businessId)) {
            throw new BusinessNotFoundException(businessId);
        }

        // Buscamos el servicio dentro del negocio concreto.
        // Si no existe o pertenece a otro negocio, devolvemos un error 404.
        return offeredServiceRepository
                .findByIdAndBusinessId(serviceId, businessId)
                .orElseThrow(() -> new com.laura.chatbot_ia.exception.OfferedServiceNotFoundException(
                        serviceId,
                        businessId));
    }

    // Actualiza los datos de un servicio sin modificar su ID ni su negocio
    // asociado.
    public OfferedService update(
            Long businessId,
            Long serviceId,
            OfferedService updatedService) {

        // Recuperamos el servicio existente y verificamos que pertenece al negocio.
        OfferedService existingService = findByIdAndBusinessId(serviceId, businessId);

        // Actualizamos únicamente los campos editables del servicio.
        existingService.setName(updatedService.getName());
        existingService.setDescription(updatedService.getDescription());
        existingService.setPrice(updatedService.getPrice());
        existingService.setDurationMinutes(updatedService.getDurationMinutes());
        existingService.setActive(updatedService.isActive());

        // Guardamos los cambios en la base de datos y devolvemos el servicio
        // actualizado.
        return offeredServiceRepository.save(existingService);
    }

}

package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.exception.OfferedServiceNotFoundException;

import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.OfferedServiceRepository;
import com.laura.chatbot_ia.service.OfferedServiceManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Probamos el gestor de servicios aislándolo de la base de datos real.
@ExtendWith(MockitoExtension.class)
class OfferedServiceManagerTest {

    @Mock
    private OfferedServiceRepository offeredServiceRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private OfferedServiceManager offeredServiceManager;

    // Verificamos que se consulta el repositorio de servicios activos.
    @Test
    void findByBusinessIdReturnsOnlyActiveServices() {
        Long businessId = 1L;
        OfferedService activeService = new OfferedService();

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByBusinessIdAndActiveTrue(businessId))
                .thenReturn(List.of(activeService));

        List<OfferedService> result = offeredServiceManager.findByBusinessId(businessId);

        assertEquals(1, result.size());
        assertSame(activeService, result.get(0));
        verify(offeredServiceRepository)
                .findByBusinessIdAndActiveTrue(businessId);
    }

    // Comprobamos que el listado no devuelve servicios cuando no hay ninguno
    // activo.
    @Test
    void findByBusinessIdReturnsEmptyListWhenNoActiveServicesExist() {
        Long businessId = 1L;

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByBusinessIdAndActiveTrue(businessId))
                .thenReturn(List.of());

        List<OfferedService> result = offeredServiceManager.findByBusinessId(businessId);

        assertEquals(0, result.size());
        verify(offeredServiceRepository)
                .findByBusinessIdAndActiveTrue(businessId);
    }

    // Comprobamos que el gestor guarda el servicio con el estado inactivo.
    @Test
    void changeActiveStatusDeactivatesService() {
        Long businessId = 1L;
        Long serviceId = 2L;

        OfferedService service = new OfferedService();
        service.setActive(true);

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(java.util.Optional.of(service));
        when(offeredServiceRepository.save(service)).thenReturn(service);

        OfferedService result = offeredServiceManager.changeActiveStatus(businessId, serviceId, false);

        assertEquals(false, result.isActive());
        verify(offeredServiceRepository).save(service);
    }

    // Comprobamos que el gestor guarda el servicio con el estado activo.
    @Test
    void changeActiveStatusActivatesService() {
        Long businessId = 1L;
        Long serviceId = 2L;

        OfferedService service = new OfferedService();
        service.setActive(false);

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(java.util.Optional.of(service));
        when(offeredServiceRepository.save(service)).thenReturn(service);

        OfferedService result = offeredServiceManager.changeActiveStatus(businessId, serviceId, true);

        assertEquals(true, result.isActive());
        verify(offeredServiceRepository).save(service);
    }

    // Comprobamos que el gestor actualiza los datos y guarda el servicio existente.
    @Test
    void updateChangesServiceDataAndSavesIt() {
        Long businessId = 1L;
        Long serviceId = 2L;

        OfferedService existingService = new OfferedService();
        existingService.setName("Servicio antiguo");

        OfferedService updatedData = new OfferedService();
        updatedData.setName("Servicio actualizado");

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(java.util.Optional.of(existingService));
        when(offeredServiceRepository.save(existingService)).thenReturn(existingService);

        OfferedService result = offeredServiceManager.update(businessId, serviceId, updatedData);

        assertEquals("Servicio actualizado", result.getName());
        verify(offeredServiceRepository).save(existingService);
    }

    // Comprobamos que actualizar un servicio inexistente provoca una excepción.
    @Test
    void updateThrowsExceptionWhenServiceDoesNotExistForBusiness() {
        Long businessId = 1L;
        Long serviceId = 99L;

        OfferedService updatedData = new OfferedService();
        updatedData.setName("Servicio actualizado");

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                OfferedServiceNotFoundException.class,
                () -> offeredServiceManager.update(businessId, serviceId, updatedData));
    }

}
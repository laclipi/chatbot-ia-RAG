package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.exception.BusinessNotFoundException;
import com.laura.chatbot_ia.exception.OfferedServiceNotFoundException;

import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.OfferedServiceRepository;
import com.laura.chatbot_ia.service.OfferedServiceManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
        service.setName("Consulta inicial");
        service.setPrice(new BigDecimal("35.00"));
        service.setDurationMinutes(30);
        service.setActive(true);

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(Optional.of(service));
        when(offeredServiceRepository.save(service)).thenReturn(service);

        OfferedService result = offeredServiceManager.changeActiveStatus(businessId, serviceId, false);

        assertEquals(false, result.isActive());
        assertEquals("Consulta inicial", result.getName());
        assertEquals(new BigDecimal("35.00"), result.getPrice());
        assertEquals(30, result.getDurationMinutes());
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

        Business business = new Business();
        business.setName("Clínica Demo");

        OfferedService existingService = new OfferedService();
        existingService.setBusiness(business);
        existingService.setName("Servicio antiguo");
        existingService.setDescription("Descripción antigua");
        existingService.setPrice(new BigDecimal("10.00"));
        existingService.setDurationMinutes(15);
        existingService.setActive(true);

        OfferedService updatedData = new OfferedService();
        updatedData.setName("Servicio actualizado");
        updatedData.setDescription("Descripción nueva");
        updatedData.setPrice(new BigDecimal("20.50"));
        updatedData.setDurationMinutes(40);
        updatedData.setActive(false);
        updatedData.setBusiness(new Business());

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(Optional.of(existingService));
        when(offeredServiceRepository.save(existingService)).thenReturn(existingService);

        OfferedService result = offeredServiceManager.update(businessId, serviceId, updatedData);

        assertEquals("Servicio actualizado", result.getName());
        assertEquals("Descripción nueva", result.getDescription());
        assertEquals(new BigDecimal("20.50"), result.getPrice());
        assertEquals(40, result.getDurationMinutes());
        assertEquals(false, result.isActive());
        assertSame(business, result.getBusiness());
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

    // Comprobamos que no se puede actualizar un servicio que no pertenece al
    // negocio indicado.
    @Test
    void updateThrowsExceptionWhenServiceDoesNotBelongToBusiness() {
        Long businessId = 1L;
        Long serviceId = 99L;

        OfferedService updatedData = new OfferedService();
        updatedData.setName("Intento de modificación");

        // Simulamos que la búsqueda conjunta no encuentra el servicio en ese negocio.
        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(java.util.Optional.empty());

        // Verificamos que se lanza una excepción y no se guarda ninguna modificación.
        org.junit.jupiter.api.Assertions.assertThrows(
                OfferedServiceNotFoundException.class,
                () -> offeredServiceManager.update(businessId, serviceId, updatedData));

        org.mockito.Mockito.verify(
                offeredServiceRepository,
                org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any(OfferedService.class));
    }

    // Comprobamos que el gestor guarda y devuelve un nuevo servicio.
    @Test
    void createSavesAndReturnsOfferedService() {
        Long businessId = 1L;

        Business business = new Business();
        business.setName("Clínica Demo");

        OfferedService service = new OfferedService();
        service.setName("Consulta inicial");

        // Simulamos que el negocio existe y que el repositorio lo devuelve.
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));
        when(offeredServiceRepository.save(service)).thenReturn(service);

        OfferedService result = offeredServiceManager.create(businessId, service);

        assertSame(service, result);
        assertSame(business, result.getBusiness());
        verify(offeredServiceRepository).save(service);
    }

    // Comprobamos que crear un servicio para un negocio inexistente lanza una
    // excepción.
    @Test
    void createThrowsExceptionWhenBusinessDoesNotExist() {
        Long businessId = 99L;

        OfferedService service = new OfferedService();
        service.setName("Consulta inicial");

        when(businessRepository.findById(businessId))
                .thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.laura.chatbot_ia.exception.BusinessNotFoundException.class,
                () -> offeredServiceManager.create(businessId, service));

        org.mockito.Mockito.verify(offeredServiceRepository, org.mockito.Mockito.never())
                .save(service);
    }

    // Comprobamos que no se pueden consultar servicios de un negocio inexistente.
    @Test
    void findByBusinessIdThrowsExceptionWhenBusinessDoesNotExist() {
        Long businessId = 99L;

        when(businessRepository.existsById(businessId)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.laura.chatbot_ia.exception.BusinessNotFoundException.class,
                () -> offeredServiceManager.findByBusinessId(businessId));

        org.mockito.Mockito.verify(
                offeredServiceRepository,
                org.mockito.Mockito.never()).findByBusinessIdAndActiveTrue(businessId);
    }

    // Comprobamos que no se busca un servicio si el negocio no existe.
    @Test
    void findByIdAndBusinessIdThrowsExceptionWhenBusinessDoesNotExist() {
        Long businessId = 99L;
        Long serviceId = 1L;

        when(businessRepository.existsById(businessId)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.laura.chatbot_ia.exception.BusinessNotFoundException.class,
                () -> offeredServiceManager.findByIdAndBusinessId(serviceId, businessId));

        org.mockito.Mockito.verify(
                offeredServiceRepository,
                org.mockito.Mockito.never()).findByIdAndBusinessId(serviceId, businessId);
    }

    // Comprobamos que el gestor elimina el servicio que pertenece al negocio.
    @Test
    void deleteRemovesExistingService() {
        Long businessId = 1L;
        Long serviceId = 2L;

        OfferedService service = new OfferedService();

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(java.util.Optional.of(service));

        offeredServiceManager.delete(businessId, serviceId);

        org.mockito.Mockito.verify(offeredServiceRepository).delete(service);
    }

    // Comprobamos que eliminar un servicio inexistente lanza una excepción.
    @Test
    void deleteThrowsExceptionWhenServiceDoesNotExist() {
        Long businessId = 1L;
        Long serviceId = 99L;

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                OfferedServiceNotFoundException.class,
                () -> offeredServiceManager.delete(businessId, serviceId));

        verify(offeredServiceRepository, never()).delete(any(OfferedService.class));
    }

    // Comprobamos que la búsqueda devuelve el servicio que pertenece al negocio.
    @Test
    void findByIdAndBusinessIdReturnsServiceWhenItBelongsToBusiness() {
        Long businessId = 1L;
        Long serviceId = 2L;

        OfferedService service = new OfferedService();
        service.setName("Consulta inicial");

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(Optional.of(service));

        OfferedService result = offeredServiceManager.findByIdAndBusinessId(serviceId, businessId);

        assertSame(service, result);
        verify(offeredServiceRepository).findByIdAndBusinessId(serviceId, businessId);
    }

    // Comprobamos que no se actualiza nada si el negocio no existe.
    @Test
    void updateThrowsExceptionWhenBusinessDoesNotExist() {
        Long businessId = 99L;
        Long serviceId = 2L;

        OfferedService updatedData = new OfferedService();
        updatedData.setName("Servicio actualizado");

        when(businessRepository.existsById(businessId)).thenReturn(false);

        assertThrows(
                BusinessNotFoundException.class,
                () -> offeredServiceManager.update(businessId, serviceId, updatedData));

        verify(offeredServiceRepository, never()).save(any(OfferedService.class));
    }

    // Comprobamos que no se elimina nada si el negocio no existe.
    @Test
    void deleteThrowsExceptionWhenBusinessDoesNotExist() {
        Long businessId = 99L;
        Long serviceId = 2L;

        when(businessRepository.existsById(businessId)).thenReturn(false);

        assertThrows(
                BusinessNotFoundException.class,
                () -> offeredServiceManager.delete(businessId, serviceId));

        verify(offeredServiceRepository, never()).delete(any(OfferedService.class));
    }

    // Comprobamos que no se cambia el estado si el servicio no pertenece al negocio.
    @Test
    void changeActiveStatusThrowsExceptionWhenServiceDoesNotExist() {
        Long businessId = 1L;
        Long serviceId = 99L;

        when(businessRepository.existsById(businessId)).thenReturn(true);
        when(offeredServiceRepository.findByIdAndBusinessId(serviceId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(
                OfferedServiceNotFoundException.class,
                () -> offeredServiceManager.changeActiveStatus(businessId, serviceId, false));

        verify(offeredServiceRepository, never()).save(any(OfferedService.class));
    }

}
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.repository.BusinessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Probamos el servicio de negocios aislándolo del repositorio real.
@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessService businessService;

    // Comprobamos que el listado devuelve lo que entrega el repositorio.
    @Test
    void findAllReturnsBusinessesFromRepository() {
        Business business = new Business();
        business.setName("Clínica Demo");

        when(businessRepository.findAll()).thenReturn(List.of(business));

        List<Business> result = businessService.findAll();

        assertEquals(1, result.size());
        assertSame(business, result.get(0));
        verify(businessRepository).findAll();
    }

    // Comprobamos que un catálogo vacío se devuelve sin transformarlo.
    @Test
    void findAllReturnsEmptyListWhenRepositoryIsEmpty() {
        when(businessRepository.findAll()).thenReturn(List.of());

        List<Business> result = businessService.findAll();

        assertTrue(result.isEmpty());
        verify(businessRepository).findAll();
    }

    // Comprobamos que la búsqueda devuelve el negocio cuando existe.
    @Test
    void findByIdReturnsBusinessWhenItExists() {
        Long businessId = 1L;
        Business business = new Business();
        business.setName("Clínica Demo");

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

        Optional<Business> result = businessService.findById(businessId);

        assertTrue(result.isPresent());
        assertSame(business, result.orElseThrow());
        verify(businessRepository).findById(businessId);
    }

    // Comprobamos que la búsqueda queda vacía cuando el negocio no existe.
    @Test
    void findByIdReturnsEmptyWhenBusinessDoesNotExist() {
        Long businessId = 99L;

        when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

        Optional<Business> result = businessService.findById(businessId);

        assertTrue(result.isEmpty());
        verify(businessRepository).findById(businessId);
    }

    // Comprobamos que guardar delega en el repositorio y devuelve el resultado.
    @Test
    void saveReturnsPersistedBusiness() {
        Business business = new Business();
        business.setName("Clínica Demo");

        when(businessRepository.save(business)).thenReturn(business);

        Business result = businessService.save(business);

        assertSame(business, result);
        verify(businessRepository).save(business);
    }
}

package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.BusinessMembership;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.AuthenticatedUserNotFoundException;
import com.laura.chatbot_ia.repository.BusinessMembershipRepository;
import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Probamos el servicio de negocios aislándolo del repositorio real.
@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessMembershipRepository businessMembershipRepository;

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

    // Comprobamos que crear un negocio guarda también la pertenencia de su autor.
    @Test
    void createSavesBusinessAndMembershipForExistingUser() {
        Business business = new Business();
        business.setName("Clínica Demo");

        User owner = new User();
        owner.setEmail("ana@clinica.es");

        Business savedBusiness = new Business();
        savedBusiness.setName("Clínica Demo");

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.of(owner));
        when(businessRepository.save(business)).thenReturn(savedBusiness);

        Business result = businessService.create(business, "  Ana@Clinica.es  ");

        assertSame(savedBusiness, result);
        verify(userRepository).findByEmail("ana@clinica.es");
        verify(businessRepository).save(business);

        ArgumentCaptor<BusinessMembership> membershipCaptor =
                ArgumentCaptor.forClass(BusinessMembership.class);
        verify(businessMembershipRepository).save(membershipCaptor.capture());

        BusinessMembership membership = membershipCaptor.getValue();
        assertSame(owner, membership.getUser());
        assertSame(savedBusiness, membership.getBusiness());
    }

    // Comprobamos que un email autenticado sin cuenta no deja un negocio guardado.
    @Test
    void createThrowsWhenAuthenticatedUserDoesNotExist() {
        Business business = new Business();
        business.setName("Clínica Demo");

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.empty());

        AuthenticatedUserNotFoundException exception = assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> businessService.create(business, "ana@clinica.es"));

        assertEquals(
                "No existe una cuenta para el usuario autenticado",
                exception.getMessage());
        verify(businessRepository, never()).save(any());
        verify(businessMembershipRepository, never()).save(any());
    }

    // Comprobamos que una identidad ausente no se busca ni se guarda.
    @Test
    void createThrowsWhenOwnerEmailIsMissing() {
        Business business = new Business();
        business.setName("Clínica Demo");

        assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> businessService.create(business, null));
        assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> businessService.create(business, "   "));

        verify(userRepository, never()).findByEmail(any());
        verify(businessRepository, never()).save(any());
        verify(businessMembershipRepository, never()).save(any());
    }
}

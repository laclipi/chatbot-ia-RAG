package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.BusinessMembership;
import com.laura.chatbot_ia.entity.User;
import com.laura.chatbot_ia.exception.AuthenticatedUserNotFoundException;
import com.laura.chatbot_ia.exception.BusinessAccessDeniedException;
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

    // Comprobamos que el listado contiene solo los negocios de las membresías del usuario.
    @Test
    void findAllReturnsBusinessesLinkedToTheAuthenticatedUser() {
        User owner = new User();
        owner.setEmail("ana@clinica.es");

        Business ownBusiness = new Business();
        ownBusiness.setName("Clínica de Ana");

        BusinessMembership membership = new BusinessMembership();
        membership.setUser(owner);
        membership.setBusiness(ownBusiness);

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.of(owner));
        when(businessMembershipRepository.findByUserId(owner.getId()))
                .thenReturn(List.of(membership));

        List<Business> result = businessService.findAll("  Ana@Clinica.es  ");

        assertEquals(1, result.size());
        assertSame(ownBusiness, result.get(0));
        verify(businessRepository, never()).findAll();
    }

    // Comprobamos que un usuario sin membresías recibe un listado vacío.
    @Test
    void findAllReturnsEmptyListWhenUserHasNoMemberships() {
        User owner = new User();
        owner.setEmail("ana@clinica.es");

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.of(owner));
        when(businessMembershipRepository.findByUserId(owner.getId())).thenReturn(List.of());

        List<Business> result = businessService.findAll("ana@clinica.es");

        assertTrue(result.isEmpty());
        verify(businessRepository, never()).findAll();
    }

    // Comprobamos que un miembro recibe su negocio.
    @Test
    void findByIdReturnsBusinessWhenUserIsMember() {
        Long businessId = 1L;
        User owner = new User();
        owner.setEmail("ana@clinica.es");
        Business business = new Business();
        business.setName("Clínica de Ana");

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.of(owner));
        when(businessMembershipRepository.existsByUserIdAndBusinessId(owner.getId(), businessId))
                .thenReturn(true);
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

        Optional<Business> result = businessService.findById(businessId, "ana@clinica.es");

        assertTrue(result.isPresent());
        assertSame(business, result.orElseThrow());
        verify(businessRepository).findById(businessId);
    }

    // Comprobamos que un negocio ajeno no se consulta ni se devuelve.
    @Test
    void findByIdThrowsWhenUserIsNotMember() {
        Long businessId = 2L;
        User owner = new User();
        owner.setEmail("ana@clinica.es");

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.of(owner));
        when(businessMembershipRepository.existsByUserIdAndBusinessId(owner.getId(), businessId))
                .thenReturn(false);

        BusinessAccessDeniedException exception = assertThrows(
                BusinessAccessDeniedException.class,
                () -> businessService.findById(businessId, "ana@clinica.es"));

        assertEquals("No perteneces a ese negocio", exception.getMessage());
        verify(businessRepository, never()).findById(any());
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

    // Comprobamos que un email con otra capitalización encuentra su membresía.
    @Test
    void requireMembershipReturnsUserWhenMembershipExists() {
        Long businessId = 4L;
        User owner = new User();
        owner.setEmail("ana@clinica.es");

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.of(owner));
        when(businessMembershipRepository.existsByUserIdAndBusinessId(owner.getId(), businessId))
                .thenReturn(true);

        User result = businessService.requireMembership("  Ana@Clinica.es  ", businessId);

        assertSame(owner, result);
        verify(userRepository).findByEmail("ana@clinica.es");
        verify(businessMembershipRepository)
                .existsByUserIdAndBusinessId(owner.getId(), businessId);
    }

    // Comprobamos que un email sin cuenta no llega a consultar la membresía.
    @Test
    void requireMembershipThrowsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.empty());

        AuthenticatedUserNotFoundException exception = assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> businessService.requireMembership("ana@clinica.es", 4L));

        assertEquals(
                "No existe una cuenta para el usuario autenticado",
                exception.getMessage());
        verify(businessMembershipRepository, never())
                .existsByUserIdAndBusinessId(any(), any());
    }

    // Comprobamos que un usuario sin pertenencia recibe un acceso denegado.
    @Test
    void requireMembershipThrowsWhenMembershipDoesNotExist() {
        Long businessId = 4L;
        User owner = new User();
        owner.setEmail("ana@clinica.es");

        when(userRepository.findByEmail("ana@clinica.es")).thenReturn(Optional.of(owner));
        when(businessMembershipRepository.existsByUserIdAndBusinessId(owner.getId(), businessId))
                .thenReturn(false);

        BusinessAccessDeniedException exception = assertThrows(
                BusinessAccessDeniedException.class,
                () -> businessService.requireMembership("ana@clinica.es", businessId));

        assertEquals("No perteneces a ese negocio", exception.getMessage());
        verify(businessMembershipRepository)
                .existsByUserIdAndBusinessId(owner.getId(), businessId);
    }

    // Comprobamos que un email ausente no se busca ni consulta membresías.
    @Test
    void requireMembershipThrowsWhenEmailIsMissing() {
        assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> businessService.requireMembership(null, 4L));
        assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> businessService.requireMembership("   ", 4L));

        verify(userRepository, never()).findByEmail(any());
        verify(businessMembershipRepository, never())
                .existsByUserIdAndBusinessId(any(), any());
    }
}

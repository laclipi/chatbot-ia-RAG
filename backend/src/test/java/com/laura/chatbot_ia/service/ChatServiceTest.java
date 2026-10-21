
// Comprobamos que ChatService obtiene el catálogo y delega la respuesta en el motor.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.exception.BusinessNotFoundException;
import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.OfferedServiceRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Verificamos la coordinación entre el catálogo y el motor de respuestas.
class ChatServiceTest {

    // Comprobamos que delega la pregunta y los servicios del negocio al motor.
    @Test
    void answerDelegatesToResponseEngine() {
        // Simulamos las dependencias para no acceder a la base de datos ni a una API.
        OfferedServiceManager manager = mock(OfferedServiceManager.class);
        ChatResponseEngine engine = mock(ChatResponseEngine.class);
        OfferedService service = mock(OfferedService.class);
        List<OfferedService> services = List.of(service);

        when(manager.findActiveCatalogForChat(1L)).thenReturn(services);
        when(engine.answer("¿Qué servicios ofrecéis?", services))
                .thenReturn("Respuesta de prueba");

        // Inyectamos las dos dependencias simuladas en el servicio bajo prueba.
        ChatService chatService = new ChatService(manager, engine);

        // Ejecutamos la consulta y comprobamos que devuelve la respuesta del motor.
        String answer = chatService.answer(1L, "¿Qué servicios ofrecéis?");

        assertEquals("Respuesta de prueba", answer);

        // Verificamos que se consultó el negocio correcto y se llamó al motor.
        verify(manager).findActiveCatalogForChat(1L);
        verify(engine).answer("¿Qué servicios ofrecéis?", services);
    }

    // Comprobamos que un catálogo vacío también se delega al motor.
    @Test
    void answerDelegatesWhenCatalogIsEmpty() {
        // Simulamos un negocio sin servicios y un motor que genera su respuesta.
        OfferedServiceManager manager = mock(OfferedServiceManager.class);
        ChatResponseEngine engine = mock(ChatResponseEngine.class);
        List<OfferedService> services = List.of();

        when(manager.findActiveCatalogForChat(1L)).thenReturn(services);
        when(engine.answer("¿Qué servicios ofrecéis?", services))
                .thenReturn(
                        "Este negocio todavía no tiene servicios activos en su catálogo.");

        // Creamos el servicio con sus dependencias simuladas.
        ChatService chatService = new ChatService(manager, engine);

        // Comprobamos que devuelve el mensaje preparado por el motor.
        String answer = chatService.answer(1L, "¿Qué servicios ofrecéis?");

        assertEquals(
                "Este negocio todavía no tiene servicios activos en su catálogo.",
                answer);
        verify(engine).answer("¿Qué servicios ofrecéis?", services);
    }

    // El chat de un negocio activo solo redacta sus servicios activos.
    @Test
    void answerIncludesOnlyActiveServicesOfTheRequestedBusiness() {
        BusinessRepository businessRepository = mock(BusinessRepository.class);
        OfferedServiceRepository offeredServiceRepository = mock(OfferedServiceRepository.class);
        OfferedServiceManager manager = new OfferedServiceManager(
                offeredServiceRepository,
                businessRepository);
        ChatService chatService = new ChatService(manager, new RuleBasedChatResponseEngine());

        Business business = new Business();
        business.setName("Clínica Demo");
        business.setActive(true);
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        // El repositorio de activos devuelve solo el servicio público de este negocio.
        OfferedService activeService = offeredService(
                "Consulta general",
                "Valoración inicial",
                "50.00",
                30);
        when(offeredServiceRepository.findByBusinessIdAndActiveTrue(1L))
                .thenReturn(List.of(activeService));

        String answer = chatService.answer(1L, "¿Qué servicios ofrecéis?");

        assertEquals(
                "Estos son los servicios disponibles de este negocio:\n"
                        + "\n- Consulta general: Valoración inicial"
                        + " | Precio: 50.00 | Duración: 30 minutos",
                answer);
        // Un servicio inactivo o de otro negocio no entra en la respuesta.
        assertFalse(answer.contains("Tratamiento interno"));
        assertFalse(answer.contains("Masaje de otra clínica"));
        verify(offeredServiceRepository).findByBusinessIdAndActiveTrue(1L);
        verify(offeredServiceRepository, never()).findByBusinessId(1L);
    }

    // Un negocio que no existe no llega al motor de respuestas.
    @Test
    void answerDoesNotRunEngineWhenBusinessDoesNotExist() {
        BusinessRepository businessRepository = mock(BusinessRepository.class);
        OfferedServiceRepository offeredServiceRepository = mock(OfferedServiceRepository.class);
        OfferedServiceManager manager = new OfferedServiceManager(
                offeredServiceRepository,
                businessRepository);
        ChatResponseEngine engine = mock(ChatResponseEngine.class);
        ChatService chatService = new ChatService(manager, engine);

        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessNotFoundException exception = assertThrows(
                BusinessNotFoundException.class,
                () -> chatService.answer(99L, "¿Qué servicios ofrecéis?"));

        assertEquals("No existe un negocio con ID 99", exception.getMessage());
        verify(engine, never()).answer(anyString(), any());
        verify(offeredServiceRepository, never()).findByBusinessIdAndActiveTrue(99L);
    }

    // Un negocio inactivo tampoco llega al motor ni consulta su catálogo.
    @Test
    void answerDoesNotRunEngineWhenBusinessIsInactive() {
        BusinessRepository businessRepository = mock(BusinessRepository.class);
        OfferedServiceRepository offeredServiceRepository = mock(OfferedServiceRepository.class);
        OfferedServiceManager manager = new OfferedServiceManager(
                offeredServiceRepository,
                businessRepository);
        ChatResponseEngine engine = mock(ChatResponseEngine.class);
        ChatService chatService = new ChatService(manager, engine);

        Business business = new Business();
        business.setName("Clínica cerrada");
        business.setActive(false);
        when(businessRepository.findById(4L)).thenReturn(Optional.of(business));

        BusinessNotFoundException exception = assertThrows(
                BusinessNotFoundException.class,
                () -> chatService.answer(4L, "¿Qué servicios ofrecéis?"));

        assertEquals("No existe un negocio con ID 4", exception.getMessage());
        verify(engine, never()).answer(anyString(), any());
        verify(offeredServiceRepository, never()).findByBusinessIdAndActiveTrue(4L);
    }

    private OfferedService offeredService(
            String name,
            String description,
            String price,
            int durationMinutes) {
        OfferedService service = new OfferedService();
        service.setName(name);
        service.setDescription(description);
        service.setPrice(new BigDecimal(price));
        service.setDurationMinutes(durationMinutes);
        service.setActive(true);
        return service;
    }
}


// Comprobamos que ChatService obtiene el catálogo y delega la respuesta en el motor.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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

        when(manager.findByBusinessId(1L)).thenReturn(services);
        when(engine.answer("¿Qué servicios ofrecéis?", services))
                .thenReturn("Respuesta de prueba");

        // Inyectamos las dos dependencias simuladas en el servicio bajo prueba.
        ChatService chatService = new ChatService(manager, engine);

        // Ejecutamos la consulta y comprobamos que devuelve la respuesta del motor.
        String answer = chatService.answer(1L, "¿Qué servicios ofrecéis?");

        assertEquals("Respuesta de prueba", answer);

        // Verificamos que se consultó el negocio correcto y se llamó al motor.
        verify(manager).findByBusinessId(1L);
        verify(engine).answer("¿Qué servicios ofrecéis?", services);
    }

    // Comprobamos que un catálogo vacío también se delega al motor.
    @Test
    void answerDelegatesWhenCatalogIsEmpty() {
        // Simulamos un negocio sin servicios y un motor que genera su respuesta.
        OfferedServiceManager manager = mock(OfferedServiceManager.class);
        ChatResponseEngine engine = mock(ChatResponseEngine.class);
        List<OfferedService> services = List.of();

        when(manager.findByBusinessId(1L)).thenReturn(services);
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
}

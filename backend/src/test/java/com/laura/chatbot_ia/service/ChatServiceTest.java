
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Comprobamos que ChatService responde usando el catálogo del negocio.
class ChatServiceTest {

    // Verificamos que muestra los servicios registrados y sus datos.
    @Test
    void answerReturnsAvailableServices() {
        // Simulamos el servicio que consulta el catálogo sin acceder a la base de
        // datos.
        OfferedServiceManager manager = mock(OfferedServiceManager.class);
        OfferedService service = mock(OfferedService.class);

        when(manager.findByBusinessId(1L)).thenReturn(List.of(service));
        when(service.getName()).thenReturn("Consulta general");
        when(service.getDescription()).thenReturn("Valoración inicial");
        when(service.getPrice()).thenReturn(new BigDecimal("50.00"));
        when(service.getDurationMinutes()).thenReturn(30);

        // Creamos el servicio bajo prueba con el catálogo simulado.
        ChatService chatService = new ChatService(manager);

        // Comprobamos que la respuesta incluye los datos del servicio registrado.
        String answer = chatService.answer(1L, "¿Qué servicios ofrecéis?");

        assertEquals(
                "Estos son los servicios disponibles de este negocio:\n"
                        + "\n- Consulta general: Valoración inicial"
                        + " | Precio: 50.00 | Duración: 30 minutos",
                answer);
    }

    // Verificamos que no inventa servicios cuando el catálogo está vacío.
    @Test
    void answerReturnsMessageWhenCatalogIsEmpty() {
        // Simulamos un negocio que todavía no tiene servicios.
        OfferedServiceManager manager = mock(OfferedServiceManager.class);
        when(manager.findByBusinessId(1L)).thenReturn(List.of());

        // Creamos el servicio bajo prueba y consultamos el catálogo vacío.
        ChatService chatService = new ChatService(manager);
        String answer = chatService.answer(1L, "¿Qué servicios ofrecéis?");

        assertEquals(
                "Este negocio todavía no tiene servicios activos en su catálogo.",
                answer);
    }

    // Comprobamos que una pregunta sobre el precio devuelve solo ese dato.
    @Test
    void answerReturnsPriceWhenAskedAboutSpecificService() {
        // Simulamos un servicio del catálogo con precio y duración conocidos.
        OfferedServiceManager manager = mock(OfferedServiceManager.class);
        OfferedService service = mock(OfferedService.class);

        when(manager.findByBusinessId(1L)).thenReturn(List.of(service));
        when(service.getName()).thenReturn("Consulta general");
        when(service.getPrice()).thenReturn(new BigDecimal("50.00"));

        ChatService chatService = new ChatService(manager);

        // Preguntamos el precio de un servicio concreto.
        String answer = chatService.answer(
                1L, "¿Cuánto cuesta la consulta general?");

        assertEquals("El precio de Consulta general es 50.00 €.", answer);
    }

    // Comprobamos que una pregunta sobre la duración devuelve solo ese dato.
    @Test
    void answerReturnsDurationWhenAskedAboutSpecificService() {
        // Simulamos un servicio del catálogo con una duración conocida.
        OfferedServiceManager manager = mock(OfferedServiceManager.class);
        OfferedService service = mock(OfferedService.class);

        when(manager.findByBusinessId(1L)).thenReturn(List.of(service));
        when(service.getName()).thenReturn("Consulta general");
        when(service.getDurationMinutes()).thenReturn(30);

        ChatService chatService = new ChatService(manager);

        // Preguntamos cuánto dura un servicio concreto.
        String answer = chatService.answer(
                1L, "¿Cuánto dura la consulta general?");

        assertEquals("La duración de Consulta general es 30 minutos.", answer);
    }

}


// Verificamos las respuestas que genera el motor local a partir del catálogo.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Comprobamos las reglas de respuesta sin acceder a PostgreSQL ni a APIs externas.
class RuleBasedChatResponseEngineTest {

    // Verificamos que muestra los servicios y sus datos cuando preguntamos por el
    // catálogo.
    @Test
    void answerReturnsAvailableServices() {
        // Creamos un motor real y simulamos un servicio del catálogo.
        RuleBasedChatResponseEngine engine = new RuleBasedChatResponseEngine();
        OfferedService service = mock(OfferedService.class);

        when(service.getName()).thenReturn("Consulta general");
        when(service.getDescription()).thenReturn("Valoración inicial");
        when(service.getPrice()).thenReturn(new BigDecimal("50.00"));
        when(service.getDurationMinutes()).thenReturn(30);

        // Generamos la respuesta usando la lista de servicios simulada.
        String answer = engine.answer(
                "¿Qué servicios ofrecéis?", List.of(service));

        assertEquals(
                "Estos son los servicios disponibles de este negocio:\n"
                        + "\n- Consulta general: Valoración inicial"
                        + " | Precio: 50.00 | Duración: 30 minutos",
                answer);
    }

    // Verificamos que informa cuando el catálogo está vacío.
    @Test
    void answerReturnsMessageWhenCatalogIsEmpty() {
        // Creamos el motor y le pasamos una lista vacía.
        RuleBasedChatResponseEngine engine = new RuleBasedChatResponseEngine();

        String answer = engine.answer("¿Qué servicios ofrecéis?", List.of());

        assertEquals(
                "Este negocio todavía no tiene servicios activos en su catálogo.",
                answer);
    }

    // Verificamos que devuelve el precio del servicio mencionado.
    @Test
    void answerReturnsPriceWhenAskedAboutSpecificService() {
        // Simulamos un servicio con un nombre y un precio conocidos.
        RuleBasedChatResponseEngine engine = new RuleBasedChatResponseEngine();
        OfferedService service = mock(OfferedService.class);

        when(service.getName()).thenReturn("Consulta general");
        when(service.getPrice()).thenReturn(new BigDecimal("50.00"));

        String answer = engine.answer(
                "¿Cuánto cuesta la consulta general?", List.of(service));

        assertEquals("El precio de Consulta general es 50.00 €.", answer);
    }

    // Verificamos que devuelve la duración del servicio mencionado.
    @Test
    void answerReturnsDurationWhenAskedAboutSpecificService() {
        // Simulamos un servicio con una duración conocida.
        RuleBasedChatResponseEngine engine = new RuleBasedChatResponseEngine();
        OfferedService service = mock(OfferedService.class);

        when(service.getName()).thenReturn("Consulta general");
        when(service.getDurationMinutes()).thenReturn(30);

        String answer = engine.answer(
                "¿Cuánto dura la consulta general?", List.of(service));

        assertEquals("La duración de Consulta general es 30 minutos.", answer);
    }
}

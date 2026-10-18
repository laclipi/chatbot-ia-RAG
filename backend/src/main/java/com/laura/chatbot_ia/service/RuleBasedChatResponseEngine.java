
// Implementa el motor local que responde preguntas usando reglas y el catálogo recibido.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import org.springframework.stereotype.Component;

import java.util.List;

// Permite que Spring detecte esta implementación de la interfaz ChatResponseEngine.
@Component
public class RuleBasedChatResponseEngine implements ChatResponseEngine {

    // Genera una respuesta a partir de la pregunta y los servicios proporcionados.
    @Override
    public String answer(String message, List<OfferedService> services) {

        // Evitamos inventar respuestas si el catálogo está vacío.
        if (services.isEmpty()) {
            return "Este negocio todavía no tiene servicios activos en su catálogo.";
        }

        // Normalizamos la pregunta para detectar las intenciones básicas.
        String question = message.toLowerCase().trim();

        boolean asksPrice = question.contains("precio")
                || question.contains("cuesta")
                || question.contains("coste");

        boolean asksDuration = question.contains("duración")
                || question.contains("duracion")
                || question.contains("cuánto dura")
                || question.contains("cuanto dura");

        // Buscamos si la pregunta menciona el nombre de algún servicio registrado.
        OfferedService matchingService = services.stream()
                .filter(service -> question.contains(
                        service.getName().toLowerCase()))
                .findFirst()
                .orElse(null);

        // Respondemos con el precio registrado del servicio mencionado.
        if (asksPrice && matchingService != null) {
            return "El precio de " + matchingService.getName()
                    + " es " + matchingService.getPrice() + " €.";
        }

        // Respondemos con la duración registrada del servicio mencionado.
        if (asksDuration && matchingService != null) {
            return "La duración de " + matchingService.getName()
                    + " es " + matchingService.getDurationMinutes()
                    + " minutos.";
        }

        // Si no detectamos una pregunta específica, mostramos el catálogo disponible.
        StringBuilder answer = new StringBuilder(
                "Estos son los servicios disponibles de este negocio:\n");

        for (OfferedService service : services) {
            answer.append("\n- ").append(service.getName());

            if (service.getDescription() != null
                    && !service.getDescription().isBlank()) {
                answer.append(": ").append(service.getDescription());
            }

            answer.append(" | Precio: ")
                    .append(service.getPrice())
                    .append(" | Duración: ")
                    .append(service.getDurationMinutes())
                    .append(" minutos");
        }

        return answer.toString();
    }
}

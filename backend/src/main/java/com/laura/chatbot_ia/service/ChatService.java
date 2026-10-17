
// Servicio que prepara respuestas del chatbot usando el catálogo del negocio.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final OfferedServiceManager offeredServiceManager;

    // Reutilizamos el servicio existente para obtener el catálogo activo.
    public ChatService(OfferedServiceManager offeredServiceManager) {
        this.offeredServiceManager = offeredServiceManager;
    }

    // Interpreta preguntas sencillas sobre precios y duración usando el catálogo
    // real.
    public String answer(Long businessId, String message) {
        List<OfferedService> services = offeredServiceManager.findByBusinessId(businessId);

        // Evitamos inventar respuestas cuando el negocio no tiene servicios.
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

        // Respondemos únicamente con el precio del servicio mencionado.
        if (asksPrice && matchingService != null) {
            return "El precio de " + matchingService.getName()
                    + " es " + matchingService.getPrice() + " €.";
        }

        // Respondemos únicamente con la duración del servicio mencionado.
        if (asksDuration && matchingService != null) {
            return "La duración de " + matchingService.getName()
                    + " es " + matchingService.getDurationMinutes()
                    + " minutos.";
        }

        // Si no identifica una pregunta específica, mostramos el catálogo disponible.
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

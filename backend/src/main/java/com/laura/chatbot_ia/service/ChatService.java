
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

    // Genera una respuesta inicial basada únicamente en los servicios del negocio.
    public String answer(Long businessId, String message) {
        List<OfferedService> services = offeredServiceManager.findByBusinessId(businessId);

        // Si el catálogo está vacío, evitamos inventar información.
        if (services.isEmpty()) {
            return "Este negocio todavía no tiene servicios activos en su catálogo.";
        }

        // Indicamos qué servicios conoce el chatbot y sus datos registrados.
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

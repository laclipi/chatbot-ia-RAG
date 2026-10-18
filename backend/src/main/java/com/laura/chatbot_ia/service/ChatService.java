
// Coordina el acceso al catálogo del negocio y delega la respuesta en un motor.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;
import org.springframework.stereotype.Service;

import java.util.List;

// Indica a Spring que esta clase contiene la lógica de coordinación del chatbot.
@Service
public class ChatService {

    private final OfferedServiceManager offeredServiceManager;
    private final ChatResponseEngine chatResponseEngine;

    // Spring inyecta el servicio del catálogo y el motor que genera las respuestas.
    public ChatService(
            OfferedServiceManager offeredServiceManager,
            ChatResponseEngine chatResponseEngine) {
        this.offeredServiceManager = offeredServiceManager;
        this.chatResponseEngine = chatResponseEngine;
    }

    // Obtiene los servicios del negocio solicitado y delega la respuesta al motor.
    public String answer(Long businessId, String message) {
        List<OfferedService> services = offeredServiceManager.findByBusinessId(businessId);

        return chatResponseEngine.answer(message, services);
    }
}

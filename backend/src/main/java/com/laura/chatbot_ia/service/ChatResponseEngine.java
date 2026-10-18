
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.entity.OfferedService;

import java.util.List;

// Define el contrato común para los motores que generarán las respuestas del chatbot.
public interface ChatResponseEngine {

    // Recibe la pregunta y los servicios autorizados del negocio para construir una
    // respuesta.
    String answer(String message, List<OfferedService> services);
}

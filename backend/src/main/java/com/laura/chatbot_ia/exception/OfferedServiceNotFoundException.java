
package com.laura.chatbot_ia.exception;

public class OfferedServiceNotFoundException extends RuntimeException {

    public OfferedServiceNotFoundException(Long serviceId, Long businessId) {
        super(
                "No existe un servicio con ID " + serviceId
                        + " para el negocio con ID " + businessId);
    }
}


package com.laura.chatbot_ia.exception;

public class BusinessNotFoundException extends RuntimeException {

    public BusinessNotFoundException(Long businessId) {
        super("No existe un negocio con ID " + businessId);
    }
}

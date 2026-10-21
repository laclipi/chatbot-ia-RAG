package com.laura.chatbot_ia.exception;

// El usuario autenticado no pertenece al negocio solicitado.
public class BusinessAccessDeniedException extends RuntimeException {

    public BusinessAccessDeniedException(String message) {
        super(message);
    }
}

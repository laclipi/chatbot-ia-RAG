package com.laura.chatbot_ia.exception;

// El token identifica un email que no tiene una cuenta en users.
public class AuthenticatedUserNotFoundException extends RuntimeException {

    public AuthenticatedUserNotFoundException() {
        super("No existe una cuenta para el usuario autenticado");
    }
}

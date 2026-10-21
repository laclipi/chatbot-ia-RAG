package com.laura.chatbot_ia.exception;

// El email no existe o la contraseña no coincide. El mensaje no distingue ambos casos.
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

// Representa un intento de registro con un email que ya pertenece a una cuenta.
package com.laura.chatbot_ia.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

    // El mensaje explica al cliente por qué no se crea la cuenta.
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}

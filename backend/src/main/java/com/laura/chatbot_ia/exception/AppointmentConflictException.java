
// Representa un conflicto con una reserva existente, como un horario ocupado.
package com.laura.chatbot_ia.exception;

public class AppointmentConflictException extends RuntimeException {

    // El mensaje explica por qué no se puede aceptar la reserva.
    public AppointmentConflictException(String message) {
        super(message);
    }
}

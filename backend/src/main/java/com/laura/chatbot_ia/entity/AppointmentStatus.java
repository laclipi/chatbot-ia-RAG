
// Define los estados permitidos para una reserva.
// Usamos un enum para evitar valores de estado arbitrarios o inconsistentes.
package com.laura.chatbot_ia.entity;

public enum AppointmentStatus {

    // La reserva se ha creado, pero todavía no está confirmada.
    PENDING,

    // La reserva está confirmada.
    CONFIRMED,

    // La reserva se ha cancelado y no debe bloquear ese horario.
    CANCELLED
}

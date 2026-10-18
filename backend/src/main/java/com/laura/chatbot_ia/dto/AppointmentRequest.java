
// Define los datos que el cliente puede enviar para solicitar una reserva.
// El precio, el estado y el negocio se determinarán en el backend.
package com.laura.chatbot_ia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AppointmentRequest {

    // Identifica el servicio que el cliente desea reservar.
    @NotNull(message = "El servicio es obligatorio")
    private Long offeredServiceId;

    // Datos de contacto necesarios para identificar al cliente.
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String customerName;

    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    private String customerEmail;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String customerPhone;

    // La fecha debe ser futura; la duración se calculará a partir del servicio.
    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    @Future(message = "La reserva debe comenzar en una fecha futura")
    private LocalDateTime startAt;

    public AppointmentRequest() {
    }

    public Long getOfferedServiceId() {
        return offeredServiceId;
    }

    public void setOfferedServiceId(Long offeredServiceId) {
        this.offeredServiceId = offeredServiceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }
}

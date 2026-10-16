
package com.laura.chatbot_ia.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// DTO de entrada: define los datos que el cliente puede enviar para crear o actualizar un servicio.
public class OfferedServiceRequest {

    // El nombre es obligatorio y admite un máximo de 150 caracteres.
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String name;

    // La descripción es opcional, con un máximo de 1000 caracteres.
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String description;

    // El precio es obligatorio y no puede ser negativo.
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal price;

    // La duración es obligatoria y debe ser de al menos un minuto.
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser de al menos 1 minuto")
    private Integer durationMinutes;

    // Devuelve el nombre recibido.
    public String getName() {
        return name;
    }

    // Permite asignar el nombre recibido.
    public void setName(String name) {
        this.name = name;
    }

    // Devuelve la descripción recibida.
    public String getDescription() {
        return description;
    }

    // Permite asignar la descripción recibida.
    public void setDescription(String description) {
        this.description = description;
    }

    // Devuelve el precio recibido.
    public BigDecimal getPrice() {
        return price;
    }

    // Permite asignar el precio recibido.
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Devuelve la duración recibida.
    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    // Permite asignar la duración recibida.
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}

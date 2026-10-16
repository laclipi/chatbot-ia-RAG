
package com.laura.chatbot_ia.dto;

import java.math.BigDecimal;

// DTO de salida: define los datos del servicio que la API devuelve al cliente.
public class OfferedServiceResponse {

    // Identificador del servicio.
    private Long id;

    // Nombre y descripción del servicio.
    private String name;
    private String description;

    // Precio y duración del servicio.
    private BigDecimal price;
    private Integer durationMinutes;

    // Indica si el servicio está activo.
    private boolean active;

    // Constructor vacío para facilitar la creación del DTO.
    public OfferedServiceResponse() {
    }

    // Getters y setters para permitir consultar y asignar los datos del DTO.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

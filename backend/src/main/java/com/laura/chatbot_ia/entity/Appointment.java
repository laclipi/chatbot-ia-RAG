
package com.laura.chatbot_ia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Representa una reserva que se guardará como registro en PostgreSQL.
@Entity
@Table(name = "appointments")
public class Appointment {

    // Identificador único generado por la base de datos.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cada reserva pertenece obligatoriamente a un negocio.
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // Servicio que ha reservado el cliente.
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "offered_service_id", nullable = false)
    private OfferedService offeredService;

    // Guardamos los datos de contacto para futuras confirmaciones y recordatorios.
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String customerName;

    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 150)
    @Column(length = 150)
    private String customerEmail;

    @Size(max = 30)
    @Column(length = 30)
    private String customerPhone;

    // Conservamos el precio acordado aunque cambie el precio del servicio.
    @NotNull
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtBooking;

    // Guardamos el inicio y el final para poder comprobar futuros solapamientos.
    @NotNull
    @Column(nullable = false)
    private LocalDateTime startAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime endAt;

    // El estado se almacena como texto legible en lugar de un número.
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // JPA necesita un constructor vacío para reconstruir la entidad.
    public Appointment() {
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public OfferedService getOfferedService() {
        return offeredService;
    }

    public void setOfferedService(OfferedService offeredService) {
        this.offeredService = offeredService;
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

    public BigDecimal getPriceAtBooking() {
        return priceAtBooking;
    }

    public void setPriceAtBooking(BigDecimal priceAtBooking) {
        this.priceAtBooking = priceAtBooking;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}

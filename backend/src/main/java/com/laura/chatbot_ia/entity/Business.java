
package com.laura.chatbot_ia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

// Indica que esta clase representa una entidad persistente de JPA.

@Entity

// Define el nombre de la tabla en PostgreSQL.
@Table(name = "businesses")
public class Business {

    // Identificador único generado por la base de datos.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre obligatorio, con un máximo de 150 caracteres.jakarta validation
    @NotBlank
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String name;

    // Correo de contacto del negocio.
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Column(length = 150)
    private String contactEmail;

    // Teléfono de contacto.
    @Column(length = 30)
    private String phoneNumber;

    // Indica si el negocio está activo.
    @Column(nullable = false)
    private boolean active = true;

    // Constructor vacío requerido por JPA.
    public Business() {
    }

    // Getters y setters para acceder y modificar los campos.

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

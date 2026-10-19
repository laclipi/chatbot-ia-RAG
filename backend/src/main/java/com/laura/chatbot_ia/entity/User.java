
package com.laura.chatbot_ia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Representa a una persona autorizada a gestionar uno o varios negocios.
@Entity
@Table(name = "users")
public class User {

    // Identificador único generado por PostgreSQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El email identifica al usuario y no puede repetirse.
    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Guardaremos aquí el hash de la contraseña, nunca la contraseña original.
    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    // Indica si la cuenta puede autenticarse.
    @Column(nullable = false)
    private boolean active = true;

    // Constructor vacío requerido por JPA.
    public User() {
    }

    // Permite consultar el identificador sin modificarlo.
    public Long getId() {
        return id;
    }

    // Permite consultar el email del usuario.
    public String getEmail() {
        return email;
    }

    // Permite asignar el email antes de guardar el usuario.
    public void setEmail(String email) {
        this.email = email;
    }

    // Devuelve el hash para que el servicio de autenticación lo verifique.
    public String getPasswordHash() {
        return passwordHash;
    }

    // Guarda el hash calculado por el mecanismo de cifrado de contraseñas.
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Permite comprobar si la cuenta está activa.
    public boolean isActive() {
        return active;
    }

    // Permite activar o desactivar una cuenta.
    public void setActive(boolean active) {
        this.active = active;
    }
}

package com.laura.chatbot_ia.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

// Vincula un usuario con un negocio sin modificar las tablas users ni businesses.
// Cada fila representa una pertenencia y es la dueña de las dos claves foráneas.
@Entity
@Table(
        name = "business_memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_business_memberships_user_business",
                columnNames = {"user_id", "business_id"}
        )
)
public class BusinessMembership {

    // Identificador propio, igual que en el resto de entidades del proyecto.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El usuario es obligatorio. LAZY evita cargar passwordHash al leer la membresía.
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // El negocio es obligatorio. Sin CascadeType, esta relación no lo crea ni lo borra.
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // Constructor vacío requerido por JPA para reconstruir la entidad.
    public BusinessMembership() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }
}

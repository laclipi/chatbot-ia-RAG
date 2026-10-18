
// Proporciona las operaciones de persistencia para las reservas.
// JpaRepository incluye guardar, buscar por ID, listar y eliminar registros.
package com.laura.chatbot_ia.repository;

import com.laura.chatbot_ia.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

// Importamos los tipos necesarios para consultar el estado y las fechas de las reservas.
import com.laura.chatbot_ia.entity.AppointmentStatus;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Comprueba si existe una reserva del mismo negocio que se solape con el
    // horario propuesto.
    // Las reservas canceladas se excluyen y los horarios consecutivos se permiten.
    boolean existsByBusinessIdAndStatusNotAndStartAtLessThanAndEndAtGreaterThan(
            Long businessId,
            AppointmentStatus status,
            LocalDateTime proposedEnd,
            LocalDateTime proposedStart);

    // Recupera únicamente las reservas asociadas al negocio indicado.
    List<Appointment> findByBusinessId(Long businessId);

    // Busca una reserva por su ID y el negocio al que pertenece.
    // Evita operar sobre una reserva de otro negocio.
    Optional<Appointment> findByIdAndBusinessId(Long id, Long businessId);
}

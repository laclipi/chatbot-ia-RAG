
// Coordina las reglas de negocio necesarias para crear una reserva.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.dto.AppointmentRequest;
import com.laura.chatbot_ia.entity.Appointment;
import com.laura.chatbot_ia.entity.AppointmentStatus;
import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.exception.AppointmentConflictException;
import com.laura.chatbot_ia.exception.BusinessNotFoundException;
import com.laura.chatbot_ia.exception.OfferedServiceNotFoundException;
import com.laura.chatbot_ia.repository.AppointmentRepository;
import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.OfferedServiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AppointmentService {

    // Repositorios para guardar reservas y consultar negocio y servicio.
    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final OfferedServiceRepository offeredServiceRepository;

    // Spring inyecta automáticamente los repositorios mediante el constructor.
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            BusinessRepository businessRepository,
            OfferedServiceRepository offeredServiceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.businessRepository = businessRepository;
        this.offeredServiceRepository = offeredServiceRepository;
    }

    // Crea una reserva después de comprobar que el negocio, el servicio y el
    // horario son válidos.
    public Appointment create(Long businessId, AppointmentRequest request) {

        // Comprobamos que el negocio exista antes de aceptar la reserva.
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));

        // Un negocio inactivo no puede aceptar nuevas reservas.
        if (!business.isActive()) {
            throw new AppointmentConflictException(
                    "El negocio no está activo y no puede aceptar reservas");
        }

        // Buscamos el servicio dentro de ese negocio y exigimos que esté activo.
        Long serviceId = request.getOfferedServiceId();

        OfferedService offeredService = offeredServiceRepository
                .findByIdAndBusinessIdAndActiveTrue(serviceId, businessId)
                .orElseThrow(() -> new OfferedServiceNotFoundException(serviceId, businessId));

        // Calculamos el final usando la duración actual del servicio.
        LocalDateTime startAt = request.getStartAt();
        LocalDateTime endAt = startAt.plusMinutes(
                offeredService.getDurationMinutes());

        // Rechazamos la reserva si ya existe otra cita que ocupa parte del horario.
        boolean overlappingAppointment = appointmentRepository
                .existsByBusinessIdAndStatusNotAndStartAtLessThanAndEndAtGreaterThan(
                        businessId,
                        AppointmentStatus.CANCELLED,
                        endAt,
                        startAt);

        if (overlappingAppointment) {
            throw new AppointmentConflictException(
                    "El horario solicitado ya está ocupado");
        }

        // Construimos la reserva con los datos validados y el precio vigente.
        Appointment appointment = new Appointment();
        appointment.setBusiness(business);
        appointment.setOfferedService(offeredService);
        appointment.setCustomerName(request.getCustomerName());
        appointment.setCustomerEmail(request.getCustomerEmail());
        appointment.setCustomerPhone(request.getCustomerPhone());
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setPriceAtBooking(offeredService.getPrice());
        appointment.setStatus(AppointmentStatus.PENDING);

        // Persistimos la reserva y devolvemos el registro guardado.
        return appointmentRepository.save(appointment);
    }

    // Devuelve las reservas de un negocio sin mezclar datos de otros negocios.
    public java.util.List<Appointment> findByBusinessId(Long businessId) {
        return appointmentRepository.findByBusinessId(businessId);
    }

    // Cancela una reserva sin eliminarla, conservando su historial en la base de
    // datos.
    public Appointment cancel(Long businessId, Long appointmentId) {

        Appointment appointment = appointmentRepository
                .findByIdAndBusinessId(appointmentId, businessId)
                .orElseThrow(() -> new AppointmentConflictException(
                        "No existe esa reserva para el negocio indicado"));

        // Evita cambiar de nuevo una reserva que ya está cancelada.
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentConflictException(
                    "La reserva ya está cancelada");
        }

        // Marcamos la reserva como cancelada; así deja de bloquear el horario.
        appointment.setStatus(AppointmentStatus.CANCELLED);

        return appointmentRepository.save(appointment);
    }
}


// Probamos AppointmentService con Mockito, sin acceder a PostgreSQL.
package com.laura.chatbot_ia.service;

import com.laura.chatbot_ia.dto.AppointmentRequest;
import com.laura.chatbot_ia.entity.Appointment;
import com.laura.chatbot_ia.entity.AppointmentStatus;
import com.laura.chatbot_ia.entity.Business;
import com.laura.chatbot_ia.entity.OfferedService;
import com.laura.chatbot_ia.repository.AppointmentRepository;
import com.laura.chatbot_ia.repository.BusinessRepository;
import com.laura.chatbot_ia.repository.OfferedServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private OfferedServiceRepository offeredServiceRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    // Comprueba que se guarda la reserva con su duración, precio y estado inicial.
    @Test
    void createSavesAppointmentWithCorrectEndTimeAndPrice() {
        Long businessId = 1L;
        Long serviceId = 2L;

        Business business = new Business();
        business.setName("Clínica Demo");

        OfferedService offeredService = new OfferedService();
        offeredService.setName("Consulta inicial");
        offeredService.setPrice(new BigDecimal("35.00"));
        offeredService.setDurationMinutes(30);

        LocalDateTime startAt = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);

        AppointmentRequest request = new AppointmentRequest();
        request.setOfferedServiceId(serviceId);
        request.setCustomerName("Cliente de prueba");
        request.setCustomerEmail("cliente@example.com");
        request.setCustomerPhone("600123456");
        request.setStartAt(startAt);

        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(offeredServiceRepository.findByIdAndBusinessIdAndActiveTrue(serviceId, businessId))
                .thenReturn(Optional.of(offeredService));

        when(appointmentRepository
                .existsByBusinessIdAndStatusNotAndStartAtLessThanAndEndAtGreaterThan(
                        businessId, AppointmentStatus.CANCELLED,
                        startAt.plusMinutes(30), startAt))
                .thenReturn(false);

        // Simula el guardado y devuelve la misma entidad recibida.
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.create(businessId, request);

        assertSame(business, result.getBusiness());
        assertSame(offeredService, result.getOfferedService());
        assertEquals("Cliente de prueba", result.getCustomerName());
        assertEquals(startAt, result.getStartAt());
        assertEquals(startAt.plusMinutes(30), result.getEndAt());
        assertEquals(new BigDecimal("35.00"), result.getPriceAtBooking());
        assertEquals(AppointmentStatus.PENDING, result.getStatus());

        verify(appointmentRepository).save(any(Appointment.class));
    }

    // Comprueba que un horario ocupado provoca un conflicto y no guarda otra
    // reserva.
    @Test
    void createThrowsConflictWhenTimeSlotIsOccupied() {
        Long businessId = 1L;
        Long serviceId = 2L;

        Business business = new Business();
        OfferedService offeredService = new OfferedService();
        offeredService.setPrice(new BigDecimal("35.00"));
        offeredService.setDurationMinutes(30);

        LocalDateTime startAt = LocalDateTime.now().plusDays(4).withSecond(0).withNano(0);

        AppointmentRequest request = new AppointmentRequest();
        request.setOfferedServiceId(serviceId);
        request.setCustomerName("Cliente de prueba");
        request.setStartAt(startAt);

        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(offeredServiceRepository.findByIdAndBusinessIdAndActiveTrue(serviceId, businessId))
                .thenReturn(Optional.of(offeredService));

        when(appointmentRepository
                .existsByBusinessIdAndStatusNotAndStartAtLessThanAndEndAtGreaterThan(
                        businessId, AppointmentStatus.CANCELLED,
                        startAt.plusMinutes(30), startAt))
                .thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.laura.chatbot_ia.exception.AppointmentConflictException.class,
                () -> appointmentService.create(businessId, request));

        // Si el horario está ocupado, no se debe persistir la nueva reserva.
        org.mockito.Mockito.verify(appointmentRepository,
                org.mockito.Mockito.never()).save(any(Appointment.class));
    }

    // Comprueba que cancelar una reserva cambia su estado y guarda el cambio.
    @Test
    void cancelChangesStatusAndSavesAppointment() {
        Long businessId = 1L;
        Long appointmentId = 10L;

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findByIdAndBusinessId(appointmentId, businessId))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.cancel(businessId, appointmentId);

        assertSame(appointment, result);
        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    // Comprueba que confirmar una reserva pendiente cambia el estado y guarda el
    // cambio.
    @Test
    void confirmChangesPendingStatusToConfirmedAndSavesAppointment() {
        Long businessId = 1L;
        Long appointmentId = 10L;

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findByIdAndBusinessId(appointmentId, businessId))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.confirm(businessId, appointmentId);

        assertSame(appointment, result);
        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());

        verify(appointmentRepository).save(appointment);
    }

    // Comprueba que una reserva cancelada no se puede confirmar ni guardar de
    // nuevo.
    @Test
    void confirmThrowsConflictWhenAppointmentIsCancelled() {
        Long businessId = 1L;
        Long appointmentId = 10L;

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findByIdAndBusinessId(appointmentId, businessId))
                .thenReturn(Optional.of(appointment));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.laura.chatbot_ia.exception.AppointmentConflictException.class,
                () -> appointmentService.confirm(businessId, appointmentId));

        // Verificamos que el estado no se persiste cuando la confirmación no es válida.
        org.mockito.Mockito.verify(
                appointmentRepository,
                org.mockito.Mockito.never())
                .save(any(Appointment.class));
    }

    // Comprueba que el servicio devuelve las reservas asociadas al negocio
    // indicado.
    @Test
    void findByBusinessIdReturnsAppointmentsForBusiness() {
        Long businessId = 1L;
        Appointment appointment = new Appointment();

        when(appointmentRepository.findByBusinessId(businessId))
                .thenReturn(java.util.List.of(appointment));

        java.util.List<Appointment> result = appointmentService.findByBusinessId(businessId);

        assertEquals(1, result.size());
        assertSame(appointment, result.get(0));

        verify(appointmentRepository).findByBusinessId(businessId);
    }

    // Comprueba que una reserva inexistente genera un error y no se guarda nada.
    @Test
    void cancelThrowsConflictWhenAppointmentDoesNotExistForBusiness() {
        Long businessId = 1L;
        Long appointmentId = 99L;

        when(appointmentRepository.findByIdAndBusinessId(appointmentId, businessId))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.laura.chatbot_ia.exception.AppointmentConflictException.class,
                () -> appointmentService.cancel(businessId, appointmentId));

        // Verificamos que no se modifica la base de datos si no encontramos la reserva.
        org.mockito.Mockito.verify(
                appointmentRepository,
                org.mockito.Mockito.never())
                .save(any(Appointment.class));
    }

}

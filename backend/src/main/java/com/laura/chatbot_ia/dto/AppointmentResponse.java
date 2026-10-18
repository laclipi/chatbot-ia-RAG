
// Define los datos que la API devolverá después de crear una reserva.
// Separa la respuesta HTTP de la entidad que JPA guarda en PostgreSQL.
package com.laura.chatbot_ia.dto;

import com.laura.chatbot_ia.entity.Appointment;
import com.laura.chatbot_ia.entity.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AppointmentResponse {

    private Long id;
    private Long businessId;
    private Long offeredServiceId;
    private String serviceName;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal priceAtBooking;
    private AppointmentStatus status;

    public AppointmentResponse() {
    }

    // Convierte la entidad persistida en una respuesta con los datos que exponemos.
    public static AppointmentResponse fromEntity(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setBusinessId(appointment.getBusiness().getId());
        response.setOfferedServiceId(appointment.getOfferedService().getId());
        response.setServiceName(appointment.getOfferedService().getName());
        response.setCustomerName(appointment.getCustomerName());
        response.setCustomerEmail(appointment.getCustomerEmail());
        response.setCustomerPhone(appointment.getCustomerPhone());
        response.setStartAt(appointment.getStartAt());
        response.setEndAt(appointment.getEndAt());
        response.setPriceAtBooking(appointment.getPriceAtBooking());
        response.setStatus(appointment.getStatus());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getOfferedServiceId() {
        return offeredServiceId;
    }

    public void setOfferedServiceId(Long offeredServiceId) {
        this.offeredServiceId = offeredServiceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public BigDecimal getPriceAtBooking() {
        return priceAtBooking;
    }

    public void setPriceAtBooking(BigDecimal priceAtBooking) {
        this.priceAtBooking = priceAtBooking;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}

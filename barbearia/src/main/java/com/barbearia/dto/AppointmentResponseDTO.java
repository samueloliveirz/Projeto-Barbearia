package com.barbearia.dto;

import com.barbearia.enums.AppointmentStatus;
import com.barbearia.enums.PaymentStatus;
import com.barbearia.model.Appointment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        String clientName,
        String barberName,
        String serviceName,
        LocalDateTime dateTime,
        AppointmentStatus status,
        PaymentStatus paymentStatus,
        BigDecimal value
) {
    public AppointmentResponseDTO(Appointment entity) {
        this(
                entity.getId(),
                entity.getClient().getName(),
                entity.getBarber().getName(),
                entity.getService().getName(),
                entity.getDateTime(),
                entity.getStatus(),
                entity.getPaymentStatus(),
                entity.getValue()
        );
    }
}
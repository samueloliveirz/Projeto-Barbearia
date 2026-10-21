package com.barbearia.dto;

import com.barbearia.enums.AppointmentStatus;
import com.barbearia.enums.PaymentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentUpdateDTO(
        @NotNull @Future LocalDateTime dateTime,
        @NotNull AppointmentStatus status,
        @NotNull PaymentStatus paymentStatus
) {}
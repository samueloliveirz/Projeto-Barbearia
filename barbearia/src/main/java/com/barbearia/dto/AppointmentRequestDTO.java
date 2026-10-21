package com.barbearia.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public record AppointmentRequestDTO(
        @NotBlank String clientName,
        @NotBlank String phone,
        @NotNull Long serviceId,
        @NotNull Long barberId,                    // <- NOVO
        @NotNull @Future LocalDateTime dateTime
) {
}
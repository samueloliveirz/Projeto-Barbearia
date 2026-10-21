package com.barbearia.dto;

import com.barbearia.enums.Plan;
import com.barbearia.model.Barbershop;

public record BarbershopResponseDTO(
        Long id,
        String name,
        String email,
        Plan plan
) {
    public BarbershopResponseDTO(Barbershop entity) {
        this(entity.getId(), entity.getName(), entity.getEmail(), entity.getPlan());
    }
}
package com.barbearia.service;

import com.barbearia.enums.ConversaEstado;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Conversa {
    private ConversaEstado estado = ConversaEstado.INICIO;
    private Long serviceId;
    private Long barberId;
    private LocalDateTime dateTime;
}
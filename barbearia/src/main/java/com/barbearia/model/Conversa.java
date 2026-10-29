package com.barbearia.model;

import com.barbearia.enums.ConversaEstado;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_conversa")
@Getter
@Setter
@NoArgsConstructor
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversaEstado estado = ConversaEstado.INICIO;

    private String nome;
    private Long serviceId;
    private Long barberId;
    private LocalDateTime dateTime;

    public void resetar() {
        this.estado = ConversaEstado.INICIO;
        this.nome = null;
        this.serviceId = null;
        this.barberId = null;
        this.dateTime = null;
    }
}
package com.barbearia.repository;

import com.barbearia.model.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {
    Optional<Conversa> findByTelefone(String telefone);
}
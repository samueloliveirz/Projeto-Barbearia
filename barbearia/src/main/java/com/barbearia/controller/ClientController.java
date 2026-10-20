package com.barbearia.controller;

import com.barbearia.dto.ClientDTO;
import com.barbearia.model.Barbershop;
import com.barbearia.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/clients")
public class ClientController {

    @Autowired
    private ClientService service;

    // TEMPORARIO: o barbershopId vem na URL (?barbershopId=1).
    // Na ETAPA 2 ele vai vir do TOKEN e ninguem vai poder forjar.

    @GetMapping
    public ResponseEntity<List<ClientDTO>> findAll(@AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findAll(barbershop.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ClientDTO> findById(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findById(id, barbershop.getId()));
    }

    @PostMapping
    public ResponseEntity<ClientDTO> insert(
            @Valid @RequestBody ClientDTO dto, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.insert(dto, barbershop.getId()));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ClientDTO> update(
            @PathVariable Long id, @Valid @RequestBody ClientDTO dto,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.update(id, dto, barbershop.getId()));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        service.deleteById(id, barbershop.getId());
        return ResponseEntity.noContent().build();
    }
}
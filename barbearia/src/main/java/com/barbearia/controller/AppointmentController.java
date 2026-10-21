package com.barbearia.controller;

import com.barbearia.dto.AppointmentRequestDTO;
import com.barbearia.dto.AppointmentResponseDTO;
import com.barbearia.dto.AppointmentUpdateDTO;
import com.barbearia.model.Barbershop;
import com.barbearia.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> findAll(
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findAll(barbershop.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<AppointmentResponseDTO> findById(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findById(id, barbershop.getId()));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> insert(
            @Valid @RequestBody AppointmentRequestDTO dto, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.insert(dto, barbershop.getId()));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody AppointmentUpdateDTO dto,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.update(id, dto, barbershop.getId()));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        service.delete(id, barbershop.getId());
        return ResponseEntity.noContent().build();
    }
}
package com.barbearia.controller;

import com.barbearia.dto.ServiceDTO;
import com.barbearia.model.Barbershop;
import com.barbearia.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/services")
public class ServiceController {

    @Autowired
    private ServiceService service;

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> findAll(@AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findAll(barbershop.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ServiceDTO> findById(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findById(id, barbershop.getId()));
    }

    @PostMapping
    public ResponseEntity<ServiceDTO> insert(
            @Valid @RequestBody ServiceDTO dto, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.insert(dto, barbershop.getId()));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ServiceDTO> update(
            @PathVariable Long id, @Valid @RequestBody ServiceDTO dto,
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
package com.barbearia.controller;

import com.barbearia.dto.BarbershopResponseDTO;
import com.barbearia.model.Barber;
import com.barbearia.model.Barbershop;
import com.barbearia.service.BarberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/barbers")
public class BarberController {

    @Autowired
    private BarberService service;

    @GetMapping
    public ResponseEntity<List<Barber>> findAll(@AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findAll(barbershop.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Barber> findById(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findById(id, barbershop.getId()));
    }

    @PostMapping
    public ResponseEntity<Barber> insert(
            @RequestParam String name, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.insert(name, barbershop.getId()));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Barber> update(
            @PathVariable Long id, @RequestParam String name,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.update(id, name, barbershop.getId()));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        service.delete(id, barbershop.getId());
        return ResponseEntity.noContent().build();
    }

 
}
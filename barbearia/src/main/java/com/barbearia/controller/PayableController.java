package com.barbearia.controller;

import com.barbearia.dto.PayableDTO;
import com.barbearia.model.Barbershop;
import com.barbearia.service.PayableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/payables")
public class PayableController {

    @Autowired
    private PayableService service;

    @GetMapping
    public ResponseEntity<List<PayableDTO>> findAll(@AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findAll(barbershop.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<PayableDTO> findById(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findById(id, barbershop.getId()));
    }

    @PostMapping
    public ResponseEntity<PayableDTO> insert(
            @Valid @RequestBody PayableDTO dto, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.insert(dto, barbershop.getId()));
    }

    @PatchMapping(value = "/{id}/pay")
    public ResponseEntity<PayableDTO> markAsPaid(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.markAsPaid(id, barbershop.getId()));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        service.delete(id, barbershop.getId());
        return ResponseEntity.noContent().build();
    }
}
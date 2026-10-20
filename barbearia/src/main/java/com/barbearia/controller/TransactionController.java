package com.barbearia.controller;

import com.barbearia.dto.TransactionDTO;
import com.barbearia.model.Barbershop;
import com.barbearia.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionController {

    @Autowired
    private TransactionService service;

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> findAll(@AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findAll(barbershop.getId()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<TransactionDTO> findById(
            @PathVariable Long id, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findById(id, barbershop.getId()));
    }

    @GetMapping(value = "/by-date")
    public ResponseEntity<List<TransactionDTO>> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findByDate(date, barbershop.getId()));
    }

    @GetMapping(value = "/by-period")
    public ResponseEntity<List<TransactionDTO>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.findByPeriod(start, end, barbershop.getId()));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> insert(
            @Valid @RequestBody TransactionDTO dto, @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.insert(dto, barbershop.getId()));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @RequestParam Long barbershopId) {
        service.delete(id, barbershopId);
        return ResponseEntity.noContent().build();
    }
}
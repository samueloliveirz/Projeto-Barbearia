package com.barbearia.controller;

import com.barbearia.model.Barbershop;
import com.barbearia.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping(value = "/finance")
public class FinanceControlle {

    @Autowired
    private FinanceService service;

    @GetMapping(value = "/revenue")
    public ResponseEntity<BigDecimal> dailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.dailyRevenue(date, barbershop.getId()));
    }

    @GetMapping(value = "/received")
    public ResponseEntity<BigDecimal> dailyReceived(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.dailyReceived(date, barbershop.getId()));
    }

    @GetMapping(value = "/credit")
    public ResponseEntity<BigDecimal> dailyCredit(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.dailyCredit(date, barbershop.getId()));
    }

    @GetMapping(value = "/owed")
    public ResponseEntity<BigDecimal> totalOwed(@AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.totalOwed(barbershop.getId()));
    }

    @GetMapping(value = "/average-ticket")
    public ResponseEntity<BigDecimal> dailyAverageTicket(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.dailyAverageTicket(date, barbershop.getId()));
    }

    @GetMapping(value = "/clients")
    public ResponseEntity<Integer> dailyClientCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.dailyClientCount(date, barbershop.getId()));
    }

    @GetMapping(value = "/expenses")
    public ResponseEntity<BigDecimal> monthlyExpenses(
            @RequestParam int month, @RequestParam int year,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.monthlyExpenses(month, year, barbershop.getId()));
    }

    @GetMapping(value = "/profit")
    public ResponseEntity<BigDecimal> monthlyProfit(
            @RequestParam int month, @RequestParam int year,
            @AuthenticationPrincipal Barbershop barbershop) {
        return ResponseEntity.ok(service.monthlyProfit(month, year, barbershop.getId()));
    }
}
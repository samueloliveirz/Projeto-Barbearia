package com.barbearia.controller;

import com.barbearia.dto.LoginDTO;
import com.barbearia.dto.RegisterDTO;
import com.barbearia.dto.TokenResponseDTO;
import com.barbearia.model.Barbershop;
import com.barbearia.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Barbershop> register(@Valid @RequestBody RegisterDTO dto) {
        Barbershop barbershop = authService.register(dto);
        return ResponseEntity.status(201).body(barbershop);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }
}
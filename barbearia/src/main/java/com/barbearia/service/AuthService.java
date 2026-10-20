package com.barbearia.service;

import com.barbearia.dto.LoginDTO;
import com.barbearia.dto.RegisterDTO;
import com.barbearia.model.Barbershop;
import com.barbearia.repository.BarbershopRepository;
import com.barbearia.service.exceptions.EmailAlreadyExistsException;
import com.barbearia.service.exceptions.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final BarbershopRepository barbershopRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(BarbershopRepository barbershopRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.barbershopRepository = barbershopRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public Barbershop register(RegisterDTO dto) {
        if (barbershopRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Já existe uma barbearia com o email: " + dto.email());
        }

        Barbershop barbershop = new Barbershop();
        barbershop.setName(dto.name());
        barbershop.setEmail(dto.email());
        barbershop.setPassword(passwordEncoder.encode(dto.password()));
        barbershop.setPlan(dto.plan());

        return barbershopRepository.save(barbershop);
    }

    public String login(LoginDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        Barbershop barbershop = barbershopRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada"));

        return jwtService.generateToken(barbershop);
    }
}
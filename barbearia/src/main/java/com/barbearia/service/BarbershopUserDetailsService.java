package com.barbearia.service;

import com.barbearia.repository.BarbershopRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BarbershopUserDetailsService implements UserDetailsService {

    private final BarbershopRepository barbershopRepository;

    public BarbershopUserDetailsService(BarbershopRepository barbershopRepository) {
        this.barbershopRepository = barbershopRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return barbershopRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Barbearia não encontrada: " + email));
    }
}

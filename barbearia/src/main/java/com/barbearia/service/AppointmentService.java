package com.barbearia.service;

import com.barbearia.dto.AppointmentDTO;
import com.barbearia.dto.AppointmentResponseDTO;
import com.barbearia.enums.AppointmentStatus;
import com.barbearia.enums.PaymentStatus;
import com.barbearia.model.*;
import com.barbearia.repository.*;
import com.barbearia.service.exceptions.HorarioIndisponivelException;
import com.barbearia.service.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository repository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private BarbershopRepository barbershopRepository;

    public List<AppointmentResponseDTO> findAll(Long barbershopId) {
        return repository.findByBarbershopId(barbershopId)
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
    }

    public AppointmentResponseDTO findById(Long id, Long barbershopId) {
        Appointment entity = repository.findByIdAndBarbershopId(id, barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return new AppointmentResponseDTO(entity);
    }


    public AppointmentResponseDTO insert(AppointmentDTO dto, Long barbershopId) {


        Barbershop shop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException(barbershopId));


        Barber barber = barberRepository.findByIdAndBarbershopId(dto.barberId(), barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException(dto.barberId()));


        verificarDisponibilidade(dto.dateTime(), barber.getId());


        Client client = findOrCreateClient(dto.clientName(), dto.phone(), shop);


        com.barbearia.model.Service service =
                serviceRepository.findByIdAndBarbershopId(dto.serviceId(), barbershopId)
                        .orElseThrow(() -> new ResourceNotFoundException(dto.serviceId()));


        Appointment obj = new Appointment();
        obj.setBarbershop(shop);                          // o tenant
        obj.setBarber(barber);                            // COM QUEM e o corte
        obj.setClient(client);
        obj.setService(service);
        obj.setDateTime(dto.dateTime());
        obj.setStatus(AppointmentStatus.CONFIRMADO); //mudar depois
        obj.setPaymentStatus(PaymentStatus.PAGO); //mudar depois
        obj.setValue(service.getPrice());

        Appointment salvo = repository.save(obj);   // era: return repository.save(obj);
        return new AppointmentResponseDTO(salvo);
    }

    public Appointment update(Long id, Appointment obj, Long barbershopId) {
        Appointment entity = repository.findByIdAndBarbershopId(id, barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (!entity.getDateTime().equals(obj.getDateTime())) {
            verificarDisponibilidade(obj.getDateTime(), entity.getBarber().getId());
        }

        entity.setDateTime(obj.getDateTime());
        entity.setStatus(obj.getStatus());
        entity.setPaymentStatus(obj.getPaymentStatus());
        entity.setValue(obj.getValue());

        return repository.save(entity);
    }

    public void delete(Long id, Long barbershopId) {
        Appointment entity = repository.findByIdAndBarbershopId(id, barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        repository.delete(entity);
    }

    private void verificarDisponibilidade(LocalDateTime dateTime, Long barberId) {
        boolean ocupado = repository.existsByDateTimeAndBarberIdAndStatusNot(
                dateTime, barberId, AppointmentStatus.CANCELADO);

        if (ocupado) {
            throw new HorarioIndisponivelException(dateTime);
        }
    }

    private Client findOrCreateClient(String name, String phone, Barbershop shop) {
        return clientRepository.findByPhoneAndBarbershopId(phone, shop.getId())
                .orElseGet(() -> {
                    Client novo = new Client();
                    novo.setName(name);
                    novo.setPhone(phone);
                    novo.setBarbershop(shop);
                    return clientRepository.save(novo);
                });
    }
}
package com.barbearia.service;

import com.barbearia.dto.AppointmentRequestDTO;
import com.barbearia.enums.ConversaEstado;
import com.barbearia.model.Barber;
import com.barbearia.repository.BarberRepository;
import com.barbearia.repository.ServiceRepository;
import com.barbearia.service.exceptions.HorarioIndisponivelException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebhookService {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Long BARBEARIA_ID = 1L;

    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;
    private final AppointmentService appointmentService;

    private final Map<String, Conversa> conversas = new HashMap<>();

    public WebhookService(ServiceRepository serviceRepository,
                          BarberRepository barberRepository,
                          AppointmentService appointmentService) {
        this.serviceRepository = serviceRepository;
        this.barberRepository = barberRepository;
        this.appointmentService = appointmentService;
    }

    public String processarMensagem(String telefone, String mensagem) {
        Conversa conversa = conversas.computeIfAbsent(telefone, k -> new Conversa());

        return switch (conversa.getEstado()) {
            case INICIO -> tratarInicio(conversa, mensagem);
            case ESCOLHENDO_SERVICO -> tratarEscolhaServico(conversa, mensagem);
            case ESCOLHENDO_BARBEIRO -> tratarEscolhaBarbeiro(conversa, mensagem);
            case ESCOLHENDO_HORARIO -> tratarEscolhaHorario(conversa, mensagem);
            case CONFIRMANDO -> tratarConfirmacao(conversa, telefone, mensagem);
        };
    }

    private String tratarInicio(Conversa conversa, String mensagem) {
        if (mensagem.equals("1")) {
            conversa.setEstado(ConversaEstado.ESCOLHENDO_SERVICO);
            return montarListaServicos();
        }
        return """
                Olá! Bem-vindo à Barbearia.
                Digite:
                1 - Agendar um horário
                2 - Ver meus agendamentos""";
    }

    private String tratarEscolhaServico(Conversa conversa, String mensagem) {
        List<com.barbearia.model.Service> servicos =
                serviceRepository.findByBarbershopId(BARBEARIA_ID);

        Integer escolha = parseOpcao(mensagem);
        if (escolha == null || escolha < 1 || escolha > servicos.size()) {
            return "Opção inválida. " + montarListaServicos();
        }

        com.barbearia.model.Service escolhido = servicos.get(escolha - 1);
        conversa.setServiceId(escolhido.getId());
        conversa.setEstado(ConversaEstado.ESCOLHENDO_BARBEIRO);

        return "Serviço: " + escolhido.getName() + "\n\n" + montarListaBarbeiros();
    }

    private String tratarEscolhaBarbeiro(Conversa conversa, String mensagem) {
        List<Barber> barbeiros = barberRepository.findByBarbershopId(BARBEARIA_ID);

        Integer escolha = parseOpcao(mensagem);
        if (escolha == null || escolha < 1 || escolha > barbeiros.size()) {
            return "Opção inválida. " + montarListaBarbeiros();
        }

        Barber escolhido = barbeiros.get(escolha - 1);
        conversa.setBarberId(escolhido.getId());
        conversa.setEstado(ConversaEstado.ESCOLHENDO_HORARIO);

        return "Barbeiro: " + escolhido.getName() +
                "\n\nQual data e horário você quer?\n(ex: 25/12/2026 14:00)";
    }

    private String tratarEscolhaHorario(Conversa conversa, String mensagem) {
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(mensagem.trim(), FORMATO);
        } catch (DateTimeParseException e) {
            return "Data inválida. Envie no formato: 25/12/2026 14:00";
        }

        if (dateTime.isBefore(LocalDateTime.now())) {
            return "Esse horário já passou. Envie uma data futura.";
        }

        conversa.setDateTime(dateTime);
        conversa.setEstado(ConversaEstado.CONFIRMANDO);

        return montarResumo(conversa);
    }

    private String tratarConfirmacao(Conversa conversa, String telefone, String mensagem) {
        String resposta = mensagem.trim();

        if (resposta.equalsIgnoreCase("sim")) {
            AppointmentRequestDTO dto = new AppointmentRequestDTO(
                    "Cliente " + telefone,
                    telefone,
                    conversa.getServiceId(),
                    conversa.getBarberId(),
                    conversa.getDateTime()
            );

            try {
                appointmentService.insert(dto, BARBEARIA_ID);
            } catch (HorarioIndisponivelException e) {
                conversas.remove(telefone);
                return "Ops, esse horário acabou de ser ocupado. Digite 1 para tentar de novo.";
            }

            conversas.remove(telefone);
            return "Agendamento confirmado! Até breve.";
        }

        if (resposta.equalsIgnoreCase("nao") || resposta.equalsIgnoreCase("não")) {
            conversas.remove(telefone);
            return "Agendamento cancelado. Digite 1 quando quiser agendar.";
        }

        return "Digite SIM para confirmar ou NAO para cancelar.";
    }

    private String montarListaServicos() {
        List<com.barbearia.model.Service> servicos =
                serviceRepository.findByBarbershopId(BARBEARIA_ID);

        StringBuilder sb = new StringBuilder("Escolha o serviço:\n");
        for (int i = 0; i < servicos.size(); i++) {
            com.barbearia.model.Service s = servicos.get(i);
            sb.append(i + 1).append(" - ").append(s.getName())
                    .append(" (R$ ").append(s.getPrice()).append(")\n");
        }
        return sb.toString();
    }

    private String montarListaBarbeiros() {
        List<Barber> barbeiros = barberRepository.findByBarbershopId(BARBEARIA_ID);

        StringBuilder sb = new StringBuilder("Escolha o barbeiro:\n");
        for (int i = 0; i < barbeiros.size(); i++) {
            sb.append(i + 1).append(" - ").append(barbeiros.get(i).getName()).append("\n");
        }
        return sb.toString();
    }

    private String montarResumo(Conversa conversa) {
        com.barbearia.model.Service servico =
                serviceRepository.findById(conversa.getServiceId()).orElseThrow();
        Barber barbeiro =
                barberRepository.findById(conversa.getBarberId()).orElseThrow();

        return """
                Confirme seu agendamento:
                Serviço: %s
                Barbeiro: %s
                Data: %s

                Digite SIM para confirmar ou NAO para cancelar.""".formatted(
                servico.getName(),
                barbeiro.getName(),
                conversa.getDateTime().format(FORMATO));
    }

    private Integer parseOpcao(String mensagem) {
        try {
            return Integer.parseInt(mensagem.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
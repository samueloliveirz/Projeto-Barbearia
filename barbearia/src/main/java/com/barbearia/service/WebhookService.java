package com.barbearia.service;

import com.barbearia.enums.ConversaEstado;
import com.barbearia.model.Barber;
import com.barbearia.repository.BarberRepository;
import com.barbearia.repository.ServiceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class WebhookService {

    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;

    private final Map<String, Conversa> conversas = new HashMap<>();
    private static final Long BARBEARIA_ID = 1L;

    public WebhookService(ServiceRepository serviceRepository, BarberRepository barberRepository) {
        this.serviceRepository = serviceRepository;
        this.barberRepository = barberRepository;
    }

    public String processarMensagem(String telefone, String mensagem) {
        Conversa conversa = conversas.computeIfAbsent(telefone, k -> new Conversa());

        return switch (conversa.getEstado()) {
            case INICIO -> tratarInicio(conversa, mensagem);
            case ESCOLHENDO_SERVICO -> tratarEscolhaServico(conversa, mensagem);
            case ESCOLHENDO_BARBEIRO -> "Estado em construção: " + conversa.getEstado();
            default -> "Estado não tratado ainda: " + conversa.getEstado();
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

    private String montarListaBarbeiros() {
        List<Barber> barbeiros = barberRepository.findByBarbershopId(BARBEARIA_ID);

        StringBuilder sb = new StringBuilder("Escolha o barbeiro:\n");
        for (int i = 0; i < barbeiros.size(); i++) {
            sb.append(i + 1).append(" - ").append(barbeiros.get(i).getName()).append("\n");
        }
        return sb.toString();
    }

    private Integer parseOpcao(String mensagem) {
        try {
            return Integer.parseInt(mensagem.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
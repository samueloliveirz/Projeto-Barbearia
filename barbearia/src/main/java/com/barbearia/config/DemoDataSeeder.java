package com.barbearia.config;

import com.barbearia.enums.AccountStatus;
import com.barbearia.enums.AppointmentStatus;
import com.barbearia.enums.PaymentStatus;
import com.barbearia.enums.Plan;
import com.barbearia.model.Appointment;
import com.barbearia.model.Barber;
import com.barbearia.model.Barbershop;
import com.barbearia.model.Client;
import com.barbearia.model.Payable;
import com.barbearia.model.Service;
import com.barbearia.repository.AppointmentRepository;
import com.barbearia.repository.BarberRepository;
import com.barbearia.repository.BarbershopRepository;
import com.barbearia.repository.ClientRepository;
import com.barbearia.repository.PayableRepository;
import com.barbearia.repository.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Mantém a barbearia de demonstração sempre pronta pra quem abrir o link.
 * A agenda é recriada quando o dia vira, pra vitrine nunca aparecer vazia.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_EMAIL = "demo@navalha.com";
    private static final String DEMO_SENHA = "demo123";

    private final BarbershopRepository barbershopRepository;
    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PayableRepository payableRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(BarbershopRepository barbershopRepository,
                          ServiceRepository serviceRepository,
                          BarberRepository barberRepository,
                          ClientRepository clientRepository,
                          AppointmentRepository appointmentRepository,
                          PayableRepository payableRepository,
                          PasswordEncoder passwordEncoder) {
        this.barbershopRepository = barbershopRepository;
        this.serviceRepository = serviceRepository;
        this.barberRepository = barberRepository;
        this.clientRepository = clientRepository;
        this.appointmentRepository = appointmentRepository;
        this.payableRepository = payableRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Barbershop demo = barbershopRepository.findByEmail(DEMO_EMAIL)
                .orElseGet(this::criarBarbearia);

        if (serviceRepository.findByBarbershopId(demo.getId()).isEmpty()) {
            criarCadastrosBase(demo);
        }

        LocalDate hoje = LocalDate.now();
        boolean jaTemAgendaHoje = !appointmentRepository
                .findByDateTimeBetweenAndBarbershopId(
                        hoje.atStartOfDay(), hoje.atTime(LocalTime.MAX), demo.getId())
                .isEmpty();

        if (!jaTemAgendaHoje) {
            criarAgendaDoDia(demo, hoje);
        }

        System.out.println("Conta demo pronta: " + DEMO_EMAIL + " / " + DEMO_SENHA);
    }

    private Barbershop criarBarbearia() {
        Barbershop shop = new Barbershop();
        shop.setName("Barbearia Navalha");
        shop.setEmail(DEMO_EMAIL);
        shop.setPassword(passwordEncoder.encode(DEMO_SENHA));
        shop.setPlan(Plan.PRO);
        return barbershopRepository.save(shop);
    }

    private void criarCadastrosBase(Barbershop shop) {
        salvarServico(shop, "Corte masculino", "35.00", 30);
        salvarServico(shop, "Corte + barba", "50.00", 50);
        salvarServico(shop, "Barba", "25.00", 20);

        salvarBarbeiro(shop, "João");
        salvarBarbeiro(shop, "Pedro");

        LocalDate hoje = LocalDate.now();
        salvarConta(shop, "Aluguel da sala", "800.00", hoje.withDayOfMonth(5), AccountStatus.PENDENTE);
        salvarConta(shop, "Energia", "130.00", hoje.plusDays(9), AccountStatus.PENDENTE);
        salvarConta(shop, "Produtos (pomada e lâmina)", "240.00", hoje.minusDays(4), AccountStatus.PAGO);
    }

    private void criarAgendaDoDia(Barbershop shop, LocalDate dia) {
        List<Service> servicos = serviceRepository.findByBarbershopId(shop.getId());
        List<Barber> barbeiros = barberRepository.findByBarbershopId(shop.getId());
        if (servicos.isEmpty() || barbeiros.isEmpty()) {
            return;
        }

        Service corte = servicos.get(0);
        Service corteBarba = servicos.size() > 1 ? servicos.get(1) : corte;
        Service barba = servicos.size() > 2 ? servicos.get(2) : corte;
        Barber joao = barbeiros.get(0);
        Barber pedro = barbeiros.size() > 1 ? barbeiros.get(1) : joao;

        agendar(shop, joao, corteBarba, "Carlos Academia", "11990001111",
                dia.atTime(9, 0), AppointmentStatus.CONCLUIDO, PaymentStatus.PAGO);
        agendar(shop, pedro, corte, "Rafa do Posto", "11990002222",
                dia.atTime(10, 30), AppointmentStatus.CONCLUIDO, PaymentStatus.PAGO);
        agendar(shop, joao, barba, "Pedro Bigode", "11990003333",
                dia.atTime(14, 0), AppointmentStatus.CONFIRMADO, PaymentStatus.PAGO);
        agendar(shop, pedro, corte, "Léo Primo", "11990004444",
                dia.atTime(15, 30), AppointmentStatus.CONFIRMADO, PaymentStatus.FIADO);
        agendar(shop, joao, corteBarba, "Diego Tatuador", "11990005555",
                dia.atTime(17, 0), AppointmentStatus.CONFIRMADO, PaymentStatus.FIADO);
    }

    private void agendar(Barbershop shop, Barber barber, Service service,
                         String nomeCliente, String telefone, LocalDateTime quando,
                         AppointmentStatus status, PaymentStatus pagamento) {
        Client client = clientRepository.findByPhoneAndBarbershopId(telefone, shop.getId())
                .orElseGet(() -> {
                    Client novo = new Client();
                    novo.setName(nomeCliente);
                    novo.setPhone(telefone);
                    novo.setBarbershop(shop);
                    return clientRepository.save(novo);
                });

        Appointment ap = new Appointment();
        ap.setBarbershop(shop);
        ap.setBarber(barber);
        ap.setClient(client);
        ap.setService(service);
        ap.setDateTime(quando);
        ap.setStatus(status);
        ap.setPaymentStatus(pagamento);
        ap.setValue(service.getPrice());
        appointmentRepository.save(ap);
    }

    private void salvarServico(Barbershop shop, String nome, String preco, int duracao) {
        Service s = new Service();
        s.setName(nome);
        s.setPrice(new BigDecimal(preco));
        s.setDurationMinutes(duracao);
        s.setBarbershop(shop);
        serviceRepository.save(s);
    }

    private void salvarBarbeiro(Barbershop shop, String nome) {
        Barber b = new Barber();
        b.setName(nome);
        b.setBarbershop(shop);
        barberRepository.save(b);
    }

    private void salvarConta(Barbershop shop, String descricao, String valor,
                             LocalDate vencimento, AccountStatus status) {
        Payable p = new Payable();
        p.setDescription(descricao);
        p.setAmount(new BigDecimal(valor));
        p.setDueDate(vencimento);
        p.setStatus(status);
        p.setBarbershop(shop);
        payableRepository.save(p);
    }
}
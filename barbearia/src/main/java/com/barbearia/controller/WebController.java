package com.barbearia.controller;

import com.barbearia.dto.PayableDTO;
import com.barbearia.enums.AccountStatus;
import com.barbearia.enums.PaymentStatus;
import com.barbearia.model.Appointment;
import com.barbearia.model.Barbershop;
import com.barbearia.model.Client;
import com.barbearia.repository.AppointmentRepository;
import com.barbearia.repository.ClientRepository;
import com.barbearia.service.FinanceService;
import com.barbearia.service.PayableService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class WebController {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final FinanceService financeService;
    private final PayableService payableService;

    public WebController(AppointmentRepository appointmentRepository,
                         ClientRepository clientRepository,
                         FinanceService financeService,
                         PayableService payableService) {
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.financeService = financeService;
        this.payableService = payableService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/painel")
    public String painel(@AuthenticationPrincipal Barbershop barbearia, Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);

        List<Appointment> agendamentos =
                appointmentRepository.findByDateTimeBetweenAndBarbershopId(inicio, fim, barbearia.getId());

        BigDecimal totalDoDia = agendamentos.stream()
                .map(Appointment::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("barbearia", barbearia);
        model.addAttribute("hoje", hoje);
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("totalDoDia", totalDoDia);
        return "painel";
    }

    @GetMapping("/painel/clientes")
    public String clientes(@AuthenticationPrincipal Barbershop barbearia, Model model) {
        List<Client> clients = clientRepository.findByBarbershopId(barbearia.getId());

        List<ClienteView> lista = clients.stream().map(c -> {
            int cortes = c.getAppointments().size();
            BigDecimal gasto = c.getAppointments().stream()
                    .map(Appointment::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ClienteView(c.getName(), c.getPhone(), cortes, gasto);
        }).toList();

        model.addAttribute("barbearia", barbearia);
        model.addAttribute("clientes", lista);
        return "clientes";
    }

    @GetMapping("/painel/caixa")
    public String caixa(@AuthenticationPrincipal Barbershop barbearia, Model model) {
        LocalDate hoje = LocalDate.now();
        Long id = barbearia.getId();

        model.addAttribute("barbearia", barbearia);
        model.addAttribute("faturadoHoje", financeService.dailyRevenue(hoje, id));
        model.addAttribute("recebidoHoje", financeService.dailyReceived(hoje, id));
        model.addAttribute("fiadoTotal", financeService.totalOwed(id));
        model.addAttribute("despesasMes", financeService.monthlyExpenses(hoje.getMonthValue(), hoje.getYear(), id));
        model.addAttribute("lucroMes", financeService.monthlyProfit(hoje.getMonthValue(), hoje.getYear(), id));
        model.addAttribute("ticketMedio", financeService.dailyAverageTicket(hoje, id));
        model.addAttribute("clientesHoje", financeService.dailyClientCount(hoje, id));
        return "caixa";
    }

    @GetMapping("/painel/contas")
    public String contas(@AuthenticationPrincipal Barbershop barbearia, Model model) {
        List<PayableDTO> contas = payableService.findAll(barbearia.getId());

        BigDecimal totalPendente = contas.stream()
                .filter(c -> c.status() != AccountStatus.PAGO)
                .map(PayableDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("barbearia", barbearia);
        model.addAttribute("contas", contas);
        model.addAttribute("totalPendente", totalPendente);
        return "contas";
    }

    @PostMapping("/painel/contas")
    public String novaConta(@AuthenticationPrincipal Barbershop barbearia,
                            @RequestParam String description,
                            @RequestParam BigDecimal amount,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        payableService.insert(new PayableDTO(null, description, amount, dueDate, null), barbearia.getId());
        return "redirect:/painel/contas";
    }

    @PostMapping("/painel/contas/{id}/pagar")
    public String pagarConta(@PathVariable Long id, @AuthenticationPrincipal Barbershop barbearia) {
        payableService.markAsPaid(id, barbearia.getId());
        return "redirect:/painel/contas";
    }

    @GetMapping("/painel/fiado")
    public String fiado(@AuthenticationPrincipal Barbershop barbearia, Model model) {
        List<Appointment> fiados = appointmentRepository
                .findByPaymentStatusAndBarbershopId(PaymentStatus.FIADO, barbearia.getId());

        model.addAttribute("barbearia", barbearia);
        model.addAttribute("fiados", fiados);
        model.addAttribute("totalFiado", financeService.totalOwed(barbearia.getId()));
        return "fiado";
    }

    @PostMapping("/painel/fiado/{id}/quitar")
    public String quitarFiado(@PathVariable Long id, @AuthenticationPrincipal Barbershop barbearia) {
        appointmentRepository.findByIdAndBarbershopId(id, barbearia.getId())
                .ifPresent(a -> {
                    a.setPaymentStatus(PaymentStatus.PAGO);
                    appointmentRepository.save(a);
                });
        return "redirect:/painel/fiado";
    }

    public record ClienteView(String nome, String telefone, int cortes, BigDecimal gasto) {}
}
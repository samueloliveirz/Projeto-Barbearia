package com.barbearia.controller;

import com.barbearia.dto.AppointmentRequestDTO;
import com.barbearia.dto.PayableDTO;
import com.barbearia.dto.RegisterDTO;
import com.barbearia.dto.ServiceDTO;
import com.barbearia.enums.AccountStatus;
import com.barbearia.enums.PaymentStatus;
import com.barbearia.model.Appointment;
import com.barbearia.model.Barbershop;
import com.barbearia.model.Client;
import com.barbearia.repository.AppointmentRepository;
import com.barbearia.repository.ClientRepository;
import com.barbearia.service.AppointmentService;
import com.barbearia.service.AuthService;
import com.barbearia.service.BarberService;
import com.barbearia.service.FinanceService;
import com.barbearia.service.PayableService;
import com.barbearia.service.ServiceService;
import com.barbearia.service.exceptions.EmailAlreadyExistsException;
import com.barbearia.service.exceptions.HorarioIndisponivelException;
import com.barbearia.service.exceptions.PlanLimitException;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    private final ServiceService serviceService;
    private final BarberService barberService;
    private final AppointmentService appointmentService;
    private final AuthService authService;

    public WebController(AppointmentRepository appointmentRepository,
                         ClientRepository clientRepository,
                         FinanceService financeService,
                         PayableService payableService,
                         ServiceService serviceService,
                         BarberService barberService,
                         AppointmentService appointmentService,
                         AuthService authService) {
        this.authService = authService;
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.financeService = financeService;
        this.payableService = payableService;
        this.serviceService = serviceService;
        this.barberService = barberService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid RegisterDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/registro?erro";
        }
        try {
            authService.register(dto);
        } catch (EmailAlreadyExistsException e) {
            return "redirect:/registro?emailExiste";
        }
        return "redirect:/login?registrado";
    }

    @GetMapping("/painel")
    public String painel(@AuthenticationPrincipal Barbershop barbearia,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
                         Model model) {
        LocalDate data = (dia != null) ? dia : LocalDate.now();
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(LocalTime.MAX);

        List<Appointment> agendamentos =
                appointmentRepository.findByDateTimeBetweenAndBarbershopId(inicio, fim, barbearia.getId());

        BigDecimal totalDoDia = agendamentos.stream()
                .map(Appointment::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("barbearia", barbearia);
        model.addAttribute("hoje", data);
        model.addAttribute("diaAnterior", data.minusDays(1));
        model.addAttribute("diaSeguinte", data.plusDays(1));
        model.addAttribute("ehHoje", data.equals(LocalDate.now()));
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

    @GetMapping("/painel/ajustes")
    public String ajustes(@AuthenticationPrincipal Barbershop barbearia, Model model) {
        model.addAttribute("barbearia", barbearia);
        model.addAttribute("servicos", serviceService.findAll(barbearia.getId()));
        model.addAttribute("barbeiros", barberService.findAll(barbearia.getId()));
        return "ajustes";
    }

    @PostMapping("/painel/ajustes/servico")
    public String novoServico(@AuthenticationPrincipal Barbershop barbearia,
                              @RequestParam String name,
                              @RequestParam BigDecimal price,
                              @RequestParam Integer durationMinutes) {
        serviceService.insert(new ServiceDTO(null, name, price, durationMinutes), barbearia.getId());
        return "redirect:/painel/ajustes";
    }

    @PostMapping("/painel/ajustes/servico/{id}/remover")
    public String removerServico(@PathVariable Long id, @AuthenticationPrincipal Barbershop barbearia) {
        serviceService.delete(id, barbearia.getId());
        return "redirect:/painel/ajustes";
    }

    @PostMapping("/painel/ajustes/barbeiro")
    public String novoBarbeiro(@AuthenticationPrincipal Barbershop barbearia, @RequestParam String name) {
        try {
            barberService.insert(name, barbearia.getId());
        } catch (PlanLimitException e) {
            return "redirect:/painel/ajustes?erroBarbeiro";
        }
        return "redirect:/painel/ajustes";
    }

    @PostMapping("/painel/ajustes/barbeiro/{id}/remover")
    public String removerBarbeiro(@PathVariable Long id, @AuthenticationPrincipal Barbershop barbearia) {
        barberService.delete(id, barbearia.getId());
        return "redirect:/painel/ajustes";
    }

    @GetMapping("/painel/agendamento/novo")
    public String novoAgendamento(@AuthenticationPrincipal Barbershop barbearia, Model model) {
        model.addAttribute("barbearia", barbearia);
        model.addAttribute("servicos", serviceService.findAll(barbearia.getId()));
        model.addAttribute("barbeiros", barberService.findAll(barbearia.getId()));
        return "agendamento";
    }

    @PostMapping("/painel/agendamento/novo")
    public String criarAgendamento(@AuthenticationPrincipal Barbershop barbearia,
                                   @RequestParam String clientName,
                                   @RequestParam String phone,
                                   @RequestParam Long serviceId,
                                   @RequestParam Long barberId,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime dateTime) {
        try {
            appointmentService.insert(
                    new AppointmentRequestDTO(clientName, phone, serviceId, barberId, dateTime), barbearia.getId());
        } catch (HorarioIndisponivelException e) {
            return "redirect:/painel/agendamento/novo?ocupado";
        }
        return "redirect:/painel";
    }

    public record ClienteView(String nome, String telefone, int cortes, BigDecimal gasto) {}
}
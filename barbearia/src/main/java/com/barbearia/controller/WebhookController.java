package com.barbearia.controller;

import com.barbearia.service.WebhookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookService webhookService;
    

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/whatsapp")
    public String receber(@RequestBody Map<String, Object> payload) {
        String telefone = (String) payload.get("telefone");
        String mensagem = (String) payload.get("mensagem");

        System.out.println("Mensagem de " + telefone + ": " + mensagem);

        return webhookService.processarMensagem(telefone, mensagem);
    }
}
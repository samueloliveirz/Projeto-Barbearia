package com.barbearia.controller;

import com.barbearia.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookService webhookService;

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping("/whatsapp")
    public ResponseEntity<String> verificar(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).build();
    }

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receber(@RequestBody Map<String, Object> payload) {
        try {
            Mensagem msg = extrairMensagem(payload);
            if (msg != null) {
                String resposta = webhookService.processarMensagem(msg.telefone(), msg.texto());
                System.out.println("Recebi de " + msg.telefone() + ": " + msg.texto());
                System.out.println("Bot responderia: " + resposta);
                // PRÓXIMO PASSO: enviar 'resposta' de volta pela Cloud API
            }
        } catch (Exception e) {
            System.out.println("Webhook ignorado (não era mensagem de texto): " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private Mensagem extrairMensagem(Map<String, Object> payload) {
        List<Map<String, Object>> entry = (List<Map<String, Object>>) payload.get("entry");
        Map<String, Object> changes =
                (Map<String, Object>) ((List<Object>) entry.get(0).get("changes")).get(0);
        Map<String, Object> value = (Map<String, Object>) changes.get("value");

        List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        Map<String, Object> message = messages.get(0);
        String telefone = (String) message.get("from");
        Map<String, Object> text = (Map<String, Object>) message.get("text");
        if (text == null) {
            return null;
        }
        return new Mensagem(telefone, (String) text.get("body"));
    }

    private record Mensagem(String telefone, String texto) {}
}
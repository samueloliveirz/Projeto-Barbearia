package com.barbearia.controller;

import com.barbearia.service.WebhookService;
import com.barbearia.service.WhatsappApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookService webhookService;
    private final WhatsappApiService whatsappApiService;

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    public WebhookController(WebhookService webhookService, WhatsappApiService whatsappApiService) {
        this.webhookService = webhookService;
        this.whatsappApiService = whatsappApiService;
    }

    // ===== META (Cloud API) — fica pro portfólio =====
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
                whatsappApiService.enviarMensagem(msg.telefone(), resposta);
            }
        } catch (Exception e) {
            System.out.println("Webhook ignorado: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // ===== TWILIO (sandbox) — pra ver funcionando =====
    @PostMapping(value = "/twilio", produces = MediaType.APPLICATION_XML_VALUE)
    public String receberTwilio(@RequestParam("From") String from,
                                @RequestParam("Body") String body) {
        String telefone = from.replace("whatsapp:", "");
        System.out.println("Twilio recebeu de " + telefone + ": " + body);

        String resposta = webhookService.processarMensagem(telefone, body);

        return "<Response><Message>" + escaparXml(resposta) + "</Message></Response>";
    }

    private String escaparXml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
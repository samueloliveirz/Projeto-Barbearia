package com.barbearia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsappApiService {

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.api-version}")
    private String apiVersion;

    private final RestClient restClient = RestClient.create();

    public void enviarMensagem(String telefone, String texto) {
        String url = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", telefone,
                "type", "text",
                "text", Map.of("body", texto)
        );

        try {
            restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            System.out.println("Resposta enviada para " + telefone);
        } catch (Exception e) {
            System.out.println("Erro ao enviar para " + telefone + ": " + e.getMessage());
        }
    }
}
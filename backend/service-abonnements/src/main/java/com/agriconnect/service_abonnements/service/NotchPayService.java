package com.agriconnect.service_abonnements.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * NotchPayService — Intégration API NotchPay
 * Gère les paiements mobiles Cameroun :
 *   - MTN Mobile Money
 *   - Orange Money
 *   - Carte bancaire
 */
@Service
@RequiredArgsConstructor
public class NotchPayService {

    private static final Logger log = LoggerFactory.getLogger(NotchPayService.class);

    @Value("${notchpay.public-key}")
    private String publicKey;

    @Value("${notchpay.secret-key}")
    private String secretKey;

    @Value("${notchpay.api-url}")
    private String apiUrl;

    @Value("${notchpay.callback-url}")
    private String callbackUrl;

    private final RestTemplate restTemplate;

    // ──────────────────────────────────────────────────────
    // INITIER UN PAIEMENT
    // ──────────────────────────────────────────────────────

    public Map<String, Object> initierPaiement(
            String email,
            int    montant,
            String description,
            String reference
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", publicKey);

            Map<String, Object> body = new HashMap<>();
            body.put("amount",      montant);
            body.put("currency",    "XAF");
            body.put("email",       email);
            body.put("description", description);
            body.put("reference",   reference);
            body.put("callback",    callbackUrl);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl + "/payments",
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED ||
                response.getStatusCode() == HttpStatus.OK) {

                Map<String, Object> responseBody = response.getBody();
                log.info("NotchPay → Paiement initié : reference={} montant={} XAF", reference, montant);

                String authorizationUrl = (String) responseBody.get("authorization_url");

                Map<String, Object> result = new HashMap<>();
                result.put("reference",        reference);
                result.put("authorizationUrl", authorizationUrl);
                result.put("statut",           "EN_ATTENTE");
                result.put("montant",          montant);
                result.put("devise",           "XAF");
                return result;
            }

            throw new RuntimeException("NotchPay erreur : " + response.getStatusCode());

        } catch (Exception e) {
            log.error("Erreur NotchPay initierPaiement : {}", e.getMessage());
            throw new RuntimeException("Erreur paiement : " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────
    // VÉRIFIER UN PAIEMENT
    // ──────────────────────────────────────────────────────

    public boolean verifierPaiement(String reference) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", secretKey);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/payments/" + reference,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body        = response.getBody();
                Map<String, Object> transaction = (Map<String, Object>) body.get("transaction");

                if (transaction != null) {
                    String statut = (String) transaction.get("status");
                    log.info("NotchPay → Vérification : reference={} statut={}", reference, statut);
                    return "complete".equalsIgnoreCase(statut);
                }
            }
            return false;

        } catch (Exception e) {
            log.error("Erreur NotchPay verifierPaiement : {}", e.getMessage());
            return false;
        }
    }

    // ──────────────────────────────────────────────────────
    // RÉFÉRENCE UNIQUE
    // ──────────────────────────────────────────────────────

    public String genererReference(Integer idUtilisateur, String categorie) {
        return "abo_" + idUtilisateur + "_" + categorie + "_" + System.currentTimeMillis();
    }
}
package com.agriconnect.service_offre.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Sélectionne le meilleur transporteur via service-suivi (Approche A).
 * service-suivi est le seul maître des données GPS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlgoTransporteurService {

    private final RestTemplate restTemplate;

    // URL vers service-suivi via Eureka load balancer
    private static final String SUIVI_URL =
        "http://service-suivi/api/suivi/transporteur-proche";

    /**
     * Appel HTTP vers service-suivi pour trouver le meilleur transporteur.
     * Retourne l'id du transporteur sélectionné, ou null si aucun disponible.
     */
    public Integer trouverTransporteur(BigDecimal poidsKg,
                                        BigDecimal latProd,
                                        BigDecimal lonProd) {
        try {
            StringBuilder url = new StringBuilder(SUIVI_URL);
            url.append("?poids=").append(poidsKg);
            if (latProd != null) url.append("&latProd=").append(latProd);
            if (lonProd != null) url.append("&lonProd=").append(lonProd);

            TransporteurProcheResponse response = restTemplate.getForObject(
                url.toString(), TransporteurProcheResponse.class);

            if (response != null && response.getIdTransporteur() != null) {
                log.info("Transporteur sélectionné via service-suivi: #{} ({}km)",
                    response.getIdTransporteur(), response.getDistanceKm());
                return response.getIdTransporteur();
            }

        } catch (Exception e) {
            log.warn("service-suivi indisponible: {} — commande en EN_ATTENTE", e.getMessage());
        }
        return null;
    }
}

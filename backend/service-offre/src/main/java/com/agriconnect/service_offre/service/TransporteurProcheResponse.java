package com.agriconnect.service_offre.service;

import lombok.*;
import java.math.BigDecimal;

/** DTO reçu depuis service-suivi */
@Data @NoArgsConstructor @AllArgsConstructor
public class TransporteurProcheResponse {
    private Integer    idTransporteur;
    private Integer    idVoiture;
    private String     nomTransporteur;
    private BigDecimal distanceKm;
    private BigDecimal chargeMax;
    private BigDecimal latActuelle;
    private BigDecimal lonActuelle;
}

package com.agriconnect.service_offre.dto;

import com.agriconnect.service_offre.model.VehiculeTransporteur.TypeVehicule;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeDTO {

    private Integer id;
    private Integer idTransporteur;

    // Infos
    private String marque;
    private String modele;
    private String immatriculation;
    private TypeVehicule typeVehicule;

    // Capacités
    private BigDecimal chargeMaxKg;
    private BigDecimal volumeMaxM3;

    // État
    private Boolean disponible;
    private Boolean enMission;
    private LocalDateTime dateAjout;
    private String photoVehicule;
}
package com.agriconnect.service_offre.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicule_transporteur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeTransporteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicule")
    private Integer id;

    @Column(name = "id_transporteur", nullable = false)
    private Integer idTransporteur;

    // Infos véhicule
    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(name = "immatriculation", nullable = false, unique = true)
    private String immatriculation;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_vehicule", nullable = false)
    private TypeVehicule typeVehicule;

    // Capacités
    @Column(name = "charge_max_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal chargeMaxKg;

    @Column(name = "volume_max_m3", precision = 8, scale = 2)
    private BigDecimal volumeMaxM3;

    // État
    @Builder.Default
    @Column(name = "disponible")
    private Boolean disponible = true;

    @Builder.Default
    @Column(name = "en_mission")
    private Boolean enMission = false;

    // Métadonnées
    @Builder.Default
    @Column(name = "date_ajout")
    private LocalDateTime dateAjout = LocalDateTime.now();

    @Column(name = "photo_vehicule")
    private String photoVehicule;

    public enum TypeVehicule {
        MOTO,
        TRICYCLE,
        CAMIONNETTE,
        CAMION,
        PICK_UP
    }
}
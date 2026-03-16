package com.agriconnect.service_utilisateurs.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transporteur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transporteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_utilisateur", unique = true, nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "num_cni", unique = true, nullable = false, length = 50)
    private String numCni;

    @Column(length = 50)
    private String permis;

    @Column(name = "photo_permis", length = 500)
    private String photoPermis;

    @Column(name = "photo_carte_grise", length = 500)
    private String photoCarteGrise;

    @Column(name = "experience_annees")
    private Integer experienceAnnees;

    @Column(name = "note_moyenne", precision = 3, scale = 2)
    private BigDecimal noteMoyenne = BigDecimal.ZERO;

    @Column(name = "nb_evaluations")
    private Integer nbEvaluations = 0;

    @Column(name = "zone_intervention", columnDefinition = "TEXT")
    private String zoneIntervention;

    @Column(name = "dispo_immediate")
    private Boolean dispoImmediate = true;
}

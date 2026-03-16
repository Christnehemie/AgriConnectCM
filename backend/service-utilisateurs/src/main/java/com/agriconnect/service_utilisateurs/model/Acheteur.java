package com.agriconnect.service_utilisateurs.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "acheteur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Acheteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_utilisateur", unique = true, nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "local_livraison", length = 255)
    private String localLivraison;

    @Column(name = "mode_paiement_prefere", length = 50)
    private String modePaiementPrefere;
}

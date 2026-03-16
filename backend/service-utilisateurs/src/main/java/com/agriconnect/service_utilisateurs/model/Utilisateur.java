package com.agriconnect.service_utilisateurs.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ============================================================
 * Utilisateur — Entité principale AgriConnect
 * ============================================================
 * L'abonnement est géré via la table abonnements (relation)
 * et non via un champ direct dans cette entité.
 * ============================================================
 */
@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "num_tel", length = 20)
    private String numTel;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "local_habitat", length = 255)
    private String localHabitat;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String mdp;

    @Column(length = 500)
    private String photo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_inscript")
    @Builder.Default
    private LocalDateTime dateInscript = LocalDateTime.now();

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    /** Rôles possibles dans AgriConnect */
    public enum Role {
        PRODUCTEUR, TRANSPORTEUR, ACHETEUR
    }
}
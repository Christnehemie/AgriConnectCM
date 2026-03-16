package com.agriconnect.service_abonnements.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


/**
 * ============================================================
 * Abonnement — Table abonnements
 * ============================================================
 * Lie un utilisateur à une catégorie d'abonnement.
 *
 * On stocke l'id utilisateur directement (Integer) sans
 * relation JPA vers service-utilisateurs — les microservices
 * ne partagent pas leurs entités.
 *
 * Statuts :
 *   ACTIF   → abonnement en cours (non expiré)
 *   EXPIRE  → date de fin dépassée (auto via verifierExpiration)
 *   ANNULE  → annulé manuellement (admin)
 * ============================================================
 */
@Entity
@Table(name = "abonnements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_a")
    private Integer id;

    /** Date de souscription — automatique à la création */
    @Builder.Default
    @Column(name = "date_abo")
    private LocalDateTime dateAbo = LocalDateTime.now();

    /**
     * Date de fin de l'abonnement.
     * null uniquement pour Gratuit (pas d'entrée en BD).
     * Calculée : dateAbo + duree jours de la catégorie.
     */
    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    /**
     * Statut de l'abonnement.
     * ACTIF par défaut à la création.
     */
    @Builder.Default
    @Column(length = 50)
    private String statut = "ACTIF";

    /**
     * ID de l'utilisateur concerné.
     * On n'utilise pas @ManyToOne vers Utilisateur car
     * c'est un microservice séparé.
     */
    @Column(name = "id_utilisateur", nullable = false)
    private Integer idUtilisateur;

    /**
     * Catégorie souscrite (Premium ou Pro).
     * EAGER car toujours nécessaire avec l'abonnement.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cat_abo", nullable = false)
    private CategorieAbonnement categorieAbonnement;
}
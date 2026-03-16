package com.agriconnect.service_abonnements.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * ============================================================
 * CategorieAbonnement — Table categories_abo
 * ============================================================
 * Données insérées par init.sql au démarrage :
 *   1 | Gratuit | 30 | 0.00
 *   2 | Premium | 30 | 29.99
 *   3 | Pro     | 30 | 99.99
 *
 * Fonctionnalités par catégorie :
 *   Gratuit → messagerie ❌  paiement ❌
 *   Premium → messagerie ✅  paiement ❌
 *   Pro     → messagerie ✅  paiement ✅
 * ============================================================
 */
@Entity
@Table(name = "categories_abo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorieAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cat_abo")
    private Integer id;

    /** Nom de la catégorie : Gratuit | Premium | Pro */
    @Column(nullable = false, length = 100)
    private String libelle;

    /** Durée en jours — 30 pour toutes les catégories */
    private Integer duree;

    /** Prix — 0.00 pour Gratuit */
    private BigDecimal prix;
}
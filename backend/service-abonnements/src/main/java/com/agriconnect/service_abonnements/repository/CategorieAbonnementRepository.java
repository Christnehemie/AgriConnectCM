package com.agriconnect.service_abonnements.repository;

import com.agriconnect.service_abonnements.model.CategorieAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository catégories d'abonnement.
 * Données insérées par init.sql — lecture seule en général.
 */
public interface CategorieAbonnementRepository
        extends JpaRepository<CategorieAbonnement, Integer> {

    /**
     * Trouve une catégorie par son libellé.
     * IgnoreCase → "premium" = "Premium" = "PREMIUM"
     */
    Optional<CategorieAbonnement> findByLibelleIgnoreCase(String libelle);
}
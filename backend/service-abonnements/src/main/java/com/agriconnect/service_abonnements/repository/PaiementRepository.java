package com.agriconnect.service_abonnements.repository;

import com.agriconnect.service_abonnements.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la table paiement.
 */
@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    /** Trouve un paiement par sa référence NotchPay */
    Optional<Paiement> findByReference(String reference);

    /** Vérifie si une référence existe déjà */
    boolean existsByReference(String reference);
}
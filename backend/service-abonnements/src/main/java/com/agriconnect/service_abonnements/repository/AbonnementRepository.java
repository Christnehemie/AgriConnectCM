package com.agriconnect.service_abonnements.repository;

import com.agriconnect.service_abonnements.model.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository abonnements utilisateur.
 */
public interface AbonnementRepository extends JpaRepository<Abonnement, Integer> {

    /**
     * Trouve l'abonnement avec un statut donné pour un utilisateur.
     * Ex: findByIdUtilisateurAndStatut(5, "ACTIF")
     * Un seul ACTIF possible à la fois.
     */
    Optional<Abonnement> findByIdUtilisateurAndStatut(
            Integer idUtilisateur, String statut);

    /**
     * Historique complet des abonnements d'un utilisateur.
     * Trié du plus récent au plus ancien.
     */
    List<Abonnement> findByIdUtilisateurOrderByDateAboDesc(
            Integer idUtilisateur);
}
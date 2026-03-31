package com.agriconnect.service_utilisateurs.repository;

import com.agriconnect.service_utilisateurs.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);

    // ✅ NOUVEAU - Trouver les transporteurs actifs avec des coordonnées
    java.util.List<Utilisateur> findByRoleAndActifTrueAndLatitudeIsNotNullAndLongitudeIsNotNull(Utilisateur.Role role);
}

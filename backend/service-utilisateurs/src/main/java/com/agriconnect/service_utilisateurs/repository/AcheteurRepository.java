package com.agriconnect.service_utilisateurs.repository;

import com.agriconnect.service_utilisateurs.model.Acheteur;
import com.agriconnect.service_utilisateurs.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AcheteurRepository extends JpaRepository<Acheteur, Integer> {
    Optional<Acheteur> findByUtilisateur(Utilisateur utilisateur);
}
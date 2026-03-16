package com.agriconnect.service_utilisateurs.repository;

import com.agriconnect.service_utilisateurs.model.Transporteur;
import com.agriconnect.service_utilisateurs.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransporteurRepository extends JpaRepository<Transporteur, Integer> {
    Optional<Transporteur> findByUtilisateur(Utilisateur utilisateur);
}
package com.agriconnect.service_utilisateurs.repository;

import com.agriconnect.service_utilisateurs.model.Producteur;
import com.agriconnect.service_utilisateurs.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProducteurRepository extends JpaRepository<Producteur, Integer> {
    Optional<Producteur> findByUtilisateur(Utilisateur utilisateur);
}
package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.Producteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProducteurRepository extends JpaRepository<Producteur, Integer> {
    Optional<Producteur> findByIdUtilisateur(Integer id);
}
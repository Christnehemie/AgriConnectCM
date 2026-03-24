package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.Transporteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransporteurRepository extends JpaRepository<Transporteur, Integer> {
    Optional<Transporteur> findByIdUtilisateur(Integer id);
}
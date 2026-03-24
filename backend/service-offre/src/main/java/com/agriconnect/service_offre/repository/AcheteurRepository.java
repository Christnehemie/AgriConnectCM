package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.Acheteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AcheteurRepository extends JpaRepository<Acheteur, Integer> {
    Optional<Acheteur> findByIdUtilisateur(Integer id);
}
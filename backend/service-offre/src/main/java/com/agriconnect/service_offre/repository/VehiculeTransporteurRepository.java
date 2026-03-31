package com.agriconnect.service_offre.repository;

import com.agriconnect.service_offre.model.VehiculeTransporteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculeTransporteurRepository extends JpaRepository<VehiculeTransporteur, Integer> {

    List<VehiculeTransporteur> findByIdTransporteurOrderByChargeMaxKgDesc(Integer idTransporteur);

    // Véhicules disponibles et pas en mission d'un transporteur
    List<VehiculeTransporteur> findByIdTransporteurAndDisponibleTrueAndEnMissionFalse(Integer idTransporteur);

    // Tous les véhicules disponibles avec capacité suffisante (pour l'algo)
    @Query("""
        SELECT v FROM VehiculeTransporteur v
        WHERE v.disponible = true
        AND v.enMission = false
        AND v.chargeMaxKg >= :poidsMin
        ORDER BY v.chargeMaxKg ASC
    """)
    List<VehiculeTransporteur> findVehiculesCapables(@Param("poidsMin") BigDecimal poidsMin);

    Optional<VehiculeTransporteur> findByImmatriculation(String immatriculation);

    boolean existsByImmatriculation(String immatriculation);

    // Compte les véhicules d'un transporteur
    long countByIdTransporteur(Integer idTransporteur);
}
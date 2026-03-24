package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.LikePublication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikePublication, LikePublication.LikeId> {
    boolean existsByIdPublicationAndIdUtilisateur(Integer idPub, Integer idUser);
    Optional<LikePublication> findByIdPublicationAndIdUtilisateur(Integer idPub, Integer idUser);
}
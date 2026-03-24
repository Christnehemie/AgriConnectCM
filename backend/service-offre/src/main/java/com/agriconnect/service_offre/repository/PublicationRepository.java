package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.Publication;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Integer> {
    Page<Publication> findAllByOrderByDatePublicationDesc(Pageable p);
    Page<Publication> findByTypePubOrderByDatePublicationDesc(String type, Pageable p);
    Page<Publication> findByTypePubAndDisponibleTrueOrderByDatePublicationDesc(String type, Pageable p);
    List<Publication> findByIdPosteurOrderByDatePublicationDesc(Integer idPosteur);

    @Query("SELECT p FROM Publication p WHERE (:type IS NULL OR p.typePub = :type) " +
           "AND LOWER(p.contenu) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "ORDER BY p.datePublication DESC")
    List<Publication> rechercher(@Param("q") String q, @Param("type") String type);
}
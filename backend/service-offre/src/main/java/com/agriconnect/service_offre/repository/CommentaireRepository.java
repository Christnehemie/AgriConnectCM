package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Integer> {
    List<Commentaire> findByIdPublicationOrderByDateCommentaireAsc(Integer id);
    long countByIdPublication(Integer id);
}
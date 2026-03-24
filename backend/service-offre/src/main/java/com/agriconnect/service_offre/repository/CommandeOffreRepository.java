package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.CommandeOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommandeOffreRepository extends JpaRepository<CommandeOffre, Integer> {
    List<CommandeOffre> findByIdAcheteurOrderByDateCommandeDesc(Integer id);
    List<CommandeOffre> findByIdProducteurOrderByDateCommandeDesc(Integer id);
    List<CommandeOffre> findByIdTransporteurOrderByDateCommandeDesc(Integer id);
}
package com.agriconnect.service_offre.repository;
import com.agriconnect.service_offre.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Integer> {
    List<Produit> findByIdProducteurOrderByNomAsc(Integer idProducteur);
}
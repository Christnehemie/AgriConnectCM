package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.model.Produit;
import com.agriconnect.service_offre.dto.*;
import com.agriconnect.service_offre.repository.*;
import com.agriconnect.service_offre.model.Producteur;
// import com.agriconnect.service_offre.repository.ProduitRepository;
// import com.agriconnect.service_offre.repository.ProducteurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository    produitRepo;
    private final ProducteurRepository producteurRepo;

    // ── Lister mes produits ──────────────────────────────────────
    public List<ProduitDTO> getMesProduits(Integer idUserProducteur) {
        Producteur p = producteurRepo.findByIdUtilisateur(idUserProducteur)
            .orElseThrow(() -> new RuntimeException("Profil producteur introuvable"));
        return produitRepo.findByIdProducteurOrderByNomAsc(p.getId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Créer un produit ─────────────────────────────────────────
    // imageUrl : URL de l'image uploadée par le frontend
    public ProduitDTO creer(Integer idUserProducteur, String nom, String descript,
                             BigDecimal prixUnitaire, BigDecimal poidsUnitaire,
                             Integer stockDisponible, LocalDate datePeremption,
                             String imageUrl) {

        Producteur producteur = producteurRepo.findByIdUtilisateur(idUserProducteur)
            .orElseThrow(() -> new RuntimeException("Profil producteur introuvable"));

        Produit produit = Produit.builder()
            .nom(nom).descript(descript)
            .prixUnitaire(prixUnitaire).poidsUnitaire(poidsUnitaire)
            .stockDisponible(stockDisponible != null ? stockDisponible : 0)
            .datePeremption(datePeremption)
            .image(imageUrl)
            .idProducteur(producteur.getId())
            .build();

        return toDTO(produitRepo.save(produit));
    }

    // ── Modifier un produit ──────────────────────────────────────
    public ProduitDTO modifier(Integer idProduit, Integer idUserProducteur,
                                String nom, String descript,
                                BigDecimal prixUnitaire, BigDecimal poidsUnitaire,
                                Integer stockDisponible, LocalDate datePeremption,
                                String imageUrl) {

        Produit produit = produitRepo.findById(idProduit)
            .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        Producteur producteur = producteurRepo.findByIdUtilisateur(idUserProducteur)
            .orElseThrow(() -> new RuntimeException("Profil producteur introuvable"));

        if (!produit.getIdProducteur().equals(producteur.getId()))
            throw new RuntimeException("Non autorisé");

        if (nom != null)             produit.setNom(nom);
        if (descript != null)        produit.setDescript(descript);
        if (prixUnitaire != null)    produit.setPrixUnitaire(prixUnitaire);
        if (poidsUnitaire != null)   produit.setPoidsUnitaire(poidsUnitaire);
        if (stockDisponible != null) produit.setStockDisponible(stockDisponible);
        if (datePeremption != null)  produit.setDatePeremption(datePeremption);
        if (imageUrl != null && !imageUrl.isBlank()) produit.setImage(imageUrl);

        return toDTO(produitRepo.save(produit));
    }

    // ── Supprimer un produit ─────────────────────────────────────
    public void supprimer(Integer idProduit, Integer idUserProducteur) {
        Produit produit = produitRepo.findById(idProduit)
            .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        Producteur producteur = producteurRepo.findByIdUtilisateur(idUserProducteur)
            .orElseThrow(() -> new RuntimeException("Profil producteur introuvable"));

        if (!produit.getIdProducteur().equals(producteur.getId()))
            throw new RuntimeException("Non autorisé");

        produitRepo.delete(produit);
    }

    // ── Mettre à jour le stock ───────────────────────────────────
    public ProduitDTO majStock(Integer idProduit, Integer idUserProducteur, Integer stock) {
        Produit produit = produitRepo.findById(idProduit)
            .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        Producteur producteur = producteurRepo.findByIdUtilisateur(idUserProducteur)
            .orElseThrow(() -> new RuntimeException("Profil producteur introuvable"));

        if (!produit.getIdProducteur().equals(producteur.getId()))
            throw new RuntimeException("Non autorisé");

        produit.setStockDisponible(stock);
        return toDTO(produitRepo.save(produit));
    }

    // ── Mapper ───────────────────────────────────────────────────
    public ProduitDTO toDTO(Produit p) {
        return ProduitDTO.builder()
            .id(p.getId()).nom(p.getNom()).descript(p.getDescript())
            .image(p.getImage()).prixUnitaire(p.getPrixUnitaire())
            .poidsUnitaire(p.getPoidsUnitaire())
            .stockDisponible(p.getStockDisponible())
            .datePeremption(p.getDatePeremption())
            .idProducteur(p.getIdProducteur())
            .build();
    }
}

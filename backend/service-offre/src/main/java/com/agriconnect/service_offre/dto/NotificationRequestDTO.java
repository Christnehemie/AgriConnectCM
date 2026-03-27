package com.agriconnect.service_offre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    
    private Integer idUtilisateur;
    private String titre;
    private String contenu;
    private String type;
    private Integer idCommande;
    private String lienAction;
    
    // Champs optionnels pour les missions
    private String nomProduit;
    private Integer quantite;
    private String uniteMesure;
    private BigDecimal prixTotal;
    private Double distanceKm;
}
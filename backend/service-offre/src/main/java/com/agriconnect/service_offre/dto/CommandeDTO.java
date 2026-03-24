package com.agriconnect.service_offre.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommandeDTO {
    private Integer id;
    private Integer idPublication;
    private String titreOffre;
    private String imageOffre;
    // Acheteur
    private Integer idAcheteur;
    private String nomAcheteur;
    private String photoAcheteur;
    private String adresseLivraison;
    // Producteur
    private Integer idProducteur;
    private String nomProducteur;
    private String photoProducteur;
    // Transporteur
    private Integer idTransporteur;
    private String nomTransporteur;
    private String photoTransporteur;
    // Commande
    private Integer quantite;
    private BigDecimal poidsTotal;
    private BigDecimal prixEstime;
    private String statut;
    private LocalDateTime dateCommande;
    private String methodePaiement;
}

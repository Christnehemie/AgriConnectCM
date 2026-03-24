package com.agriconnect.service_offre.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicationDTO {
    private Integer id;
    private String contenu;
    private String image;
    private LocalDateTime datePublication;
    private Integer nbLikes;
    private long nbCommentaires;
    private boolean likeParMoi;
    private long nbCommandes;
    // Posteur
    private Integer idPosteur;
    private String nomPosteur;
    private String photoPosteur;
    private String localisation;
    private String rolePosteur;
    // Type
    private String typePub;
    // Offre
    private Integer idProduit;
    private String nomProduit;
    private String imageProduit;
    private Integer quantiteOfferte;
    private BigDecimal prixOffre;
    private String uniteMesure;
    private Boolean disponible;
    private BigDecimal poidsUnitaire;
}

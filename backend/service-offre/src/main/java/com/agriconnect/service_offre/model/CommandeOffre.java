package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="commande_offre")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommandeOffre {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_commande")     private Integer id;
    @Column(name="id_publication")  private Integer idPublication;
    @Column(name="id_acheteur")     private Integer idAcheteur;
    @Column(name="id_producteur")   private Integer idProducteur;
    private Integer quantite;
    @Column(name="poids_total")     private BigDecimal poidsTotal;
    @Column(name="volume_total")    private BigDecimal volumeTotal;
    @Column(name="prix_estime")     private BigDecimal prixEstime;
    @Column(name="id_colis")        private Integer idColis;
    @Column(name="id_transporteur") private Integer idTransporteur;
    @Builder.Default private String statut = "EN_ATTENTE";
    @Builder.Default @Column(name="date_commande")
    private LocalDateTime dateCommande = LocalDateTime.now();
    private String notes;
}
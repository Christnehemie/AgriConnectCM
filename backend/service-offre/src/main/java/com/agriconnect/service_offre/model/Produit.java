package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="produits")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Produit {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_pro") private Integer id;
    private String nom;
    private String descript;
    private String image;
    @Column(name="prix_unitaire")    private BigDecimal prixUnitaire;
    @Column(name="poids_unitaire")   private BigDecimal poidsUnitaire;
    @Builder.Default @Column(name="stock_disponible") private Integer stockDisponible = 0;
    @Column(name="date_peremption")  private LocalDate datePeremption;
    @Column(name="id_producteur")    private Integer idProducteur;
}
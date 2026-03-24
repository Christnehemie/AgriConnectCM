package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="publications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Publication {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_p") private Integer id;
    private String contenu;
    private String image;
    @Builder.Default @Column(name="date_publication")
    private LocalDateTime datePublication = LocalDateTime.now();
    @Builder.Default @Column(name="nb_likes") private Integer nbLikes = 0;
    @Column(name="id_posteur") private Integer idPosteur;
    @Builder.Default @Column(name="type_pub") private String typePub = "POST";
    @Column(name="id_produit") private Integer idProduit;
    @Column(name="quantite_offerte") private Integer quantiteOfferte;
    @Column(name="prix_offre") private BigDecimal prixOffre;
    @Column(name="unite_mesure") private String uniteMesure;
    @Builder.Default @Column(name="disponible") private Boolean disponible = true;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="id_posteur", insertable=false, updatable=false)
    private Utilisateur posteur;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="id_produit", insertable=false, updatable=false)
    private Produit produit;
}
package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="producteur")
@Data @NoArgsConstructor @AllArgsConstructor
public class Producteur {
    @Id private Integer id;
    @Column(name="id_utilisateur")       private Integer idUtilisateur;
    @Column(name="local_cult")           private String localCult;
    @Column(name="certification_bio")    private Boolean certificationBio;
    @Column(name="description_activite") private String descriptionActivite;
    @Column(name="photo_exploitation")   private String photoExploitation;
    @Column(name="logo_entreprise")      private String logoEntreprise;
}
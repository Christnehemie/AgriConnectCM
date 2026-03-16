package com.agriconnect.service_utilisateurs.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producteur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_utilisateur", unique = true, nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "local_cult", length = 255)
    private String localCult;

    @Column(name = "photo_exploitation", length = 500)
    private String photoExploitation;

    @Column(name = "logo_entreprise", length = 500)
    private String logoEntreprise;

    @Column(name = "certification_bio")
    private Boolean certificationBio = false;

    @Column(name = "site_web", length = 255)
    private String siteWeb;

    @Column(name = "description_activite", columnDefinition = "TEXT")
    private String descriptionActivite;
}

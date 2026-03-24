package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="acheteur")
@Data @NoArgsConstructor @AllArgsConstructor
public class Acheteur {
    @Id private Integer id;
    @Column(name="id_utilisateur")        private Integer idUtilisateur;
    @Column(name="local_livraison")       private String localLivraison;
    @Column(name="mode_paiement_prefere") private String modePaiementPrefere;
}
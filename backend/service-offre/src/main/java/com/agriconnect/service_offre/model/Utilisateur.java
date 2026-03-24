package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="utilisateur")
@Data @NoArgsConstructor @AllArgsConstructor
public class Utilisateur {
    @Id private Integer id;
    private String nom;
    private String prenom;
    private String photo;
    @Column(name="local_habitat") private String localHabitat;
    private String role;
    @Column(precision=10, scale=8) private BigDecimal latitude;
    @Column(precision=11, scale=8) private BigDecimal longitude;
}
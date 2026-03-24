package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="transporteur")
@Data @NoArgsConstructor @AllArgsConstructor
public class Transporteur {
    @Id private Integer id;
    @Column(name="id_utilisateur") private Integer idUtilisateur;
    @Column(name="dispo_immediate") private Boolean dispoImmediate;
}
package com.agriconnect.service_offre.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_publication")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SavedPublication.SavedId.class)
public class SavedPublication {

    @Id
    @Column(name = "id_p")
    private Integer idPublication;

    @Id
    @Column(name = "id_utilisateur")
    private Integer idUtilisateur;

    @Builder.Default
    @Column(name = "date_enregistrement")
    private LocalDateTime dateEnregistrement = LocalDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavedId implements Serializable {
        private Integer idPublication;
        private Integer idUtilisateur;
    }
}

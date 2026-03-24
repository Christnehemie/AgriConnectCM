package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity @Table(name="like_publication")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@IdClass(LikePublication.LikeId.class)
public class LikePublication {
    @Id @Column(name="id_p")           private Integer idPublication;
    @Id @Column(name="id_utilisateur") private Integer idUtilisateur;
    @Builder.Default @Column(name="date_like")
    private LocalDateTime dateLike = LocalDateTime.now();

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LikeId implements Serializable {
        private Integer idPublication;
        private Integer idUtilisateur;
    }
}
package com.agriconnect.service_offre.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="commentaire")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Commentaire {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_commentaire") private Integer id;
    private String contenu;
    @Builder.Default @Column(name="date_commentaire")
    private LocalDateTime dateCommentaire = LocalDateTime.now();
    @Column(name="id_p")           private Integer idPublication;
    @Column(name="id_utilisateur") private Integer idUtilisateur;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="id_utilisateur", insertable=false, updatable=false)
    private Utilisateur auteur;
}
package com.agriconnect.service_offre.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentaireDTO {
    private Integer id;
    private String contenu;
    private LocalDateTime dateCommentaire;
    private Integer idAuteur;
    private String nomAuteur;
    private String photoAuteur;
}

package com.agriconnect.service_messagerie.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * Message — Table messages
 * ============================================================
 * Mappe la table existante + colonnes ajoutées par ddl-auto:update
 *
 * Types : TEXTE | IMAGE | VIDEO | FICHIER | AUDIO
 * Statuts : ENVOYE | DISTRIBUE | LU
 * ============================================================
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_me")
    private Integer id;

    /** Contenu texte du message */
    @Column(name = "contenu", columnDefinition = "TEXT")
    private String contenu;

    /** URL Cloudinary pour image/video/fichier/audio */
    @Column(name = "piece_jointe", length = 500)
    private String pieceJointe;

    @Builder.Default
    @Column(name = "date_create")
    private LocalDateTime dateCreate = LocalDateTime.now();

    /** Conversation à laquelle appartient ce message */
    @Column(name = "id_co", nullable = false)
    private Integer idConversation;

    /** Expéditeur du message */
    @Column(name = "id_sender", nullable = false)
    private Integer idSender;

    // ── Colonnes ajoutées automatiquement par ddl-auto:update ──

    /**
     * Type du message.
     * TEXTE | IMAGE | VIDEO | FICHIER | AUDIO
     */
    @Builder.Default
    @Column(name = "type_message", length = 20)
    private String typeMessage = "TEXTE";

    /**
     * Statut du message.
     * ENVOYE → DISTRIBUE → LU
     */
    @Builder.Default
    @Column(name = "statut", length = 20)
    private String statut = "ENVOYE";

    /**
     * Supprimé pour l'expéditeur uniquement.
     * true → n'apparaît plus dans sa liste
     */
    @Builder.Default
    @Column(name = "supprime_sender")
    private Boolean supprimeSender = false;

    /**
     * Supprimé pour le destinataire uniquement.
     */
    @Builder.Default
    @Column(name = "supprime_receiver")
    private Boolean supprimerReceiver = false;

    /**
     * Supprimé pour tout le monde.
     * true → affiche "Message supprimé" pour les 2
     */
    @Builder.Default
    @Column(name = "supprime_tous")
    private Boolean supprimeTous = false;

    /**
     * ID du message auquel on répond (reply).
     * null si pas de reply.
     */
    @Column(name = "id_reply")
    private Integer idReply;

    /**
     * Réactions emoji.
     * Stocké en JSON : {"❤️":[1,2],"👍":[3]}
     * Clé = emoji, Valeur = liste des idUtilisateur
     */
    @Column(name = "reactions", columnDefinition = "TEXT")
    private String reactions;
}
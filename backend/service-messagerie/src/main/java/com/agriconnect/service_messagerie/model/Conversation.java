package com.agriconnect.service_messagerie.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_co")
    private Integer id;

    @Column(name = "id_sender", nullable = false)
    private Integer idSender;

    @Column(name = "id_receiver", nullable = false)
    private Integer idReceiver;

    @Builder.Default
    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Builder.Default
    @Column(name = "type", length = 20)
    private String type = "PRIVE";

    // ✅ Épinglé par sender
    @Builder.Default
    @Column(name = "epingle_sender")
    private Boolean epingleSender = false;

    // ✅ Épinglé par receiver
    @Builder.Default
    @Column(name = "epingle_receiver")
    private Boolean epingleReceiver = false;

    // ✅ Archivé par sender
    @Builder.Default
    @Column(name = "archive_sender")
    private Boolean archiveSender = false;

    // ✅ Archivé par receiver
    @Builder.Default
    @Column(name = "archive_receiver")
    private Boolean archiveReceiver = false;

    // ✅ Supprimé par sender
    @Builder.Default
    @Column(name = "supprime_sender")
    private Boolean supprimeSender = false;

    // ✅ Supprimé par receiver
    @Builder.Default
    @Column(name = "supprime_receiver")
    private Boolean supprimeReceiver = false;
}
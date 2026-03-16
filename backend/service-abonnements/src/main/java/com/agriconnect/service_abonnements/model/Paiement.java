package com.agriconnect.service_abonnements.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * Paiement — Table paiement
 * ============================================================
 * Enregistre la trace de chaque paiement NotchPay.
 *
 * Pour un abonnement :
 *   - id_sender        → idUtilisateur qui paie
 *   - id_receiver      → null (pas d'utilisateur destinataire)
 *   - raison           → "Abonnement Premium" ou "Abonnement Pro"
 *   - statut           → EN_ATTENTE → COMPLETE ou ECHEC
 *   - id_a             → lien vers l'abonnement créé après confirmation
 *   - id_c             → null (pas de colis)
 *
 * Statuts :
 *   EN_ATTENTE → paiement initié, en attente de confirmation NotchPay
 *   COMPLETE   → paiement confirmé, abonnement activé
 *   ECHEC      → paiement échoué ou annulé
 * ============================================================
 */
@Entity
@Table(name = "paiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paie")
    private Integer id;

    /**
     * Référence unique NotchPay.
     * Format : abo_{idUtilisateur}_{categorie}_{timestamp}
     * Exemple : abo_10_Premium_1710000000
     */
    @Column(name = "reference", unique = true, nullable = false, length = 100)
    private String reference;

    /** Montant en XAF — 2000 pour Premium, 5000 pour Pro */
    @Column(name = "montant", nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    /**
     * ID de l'utilisateur qui paie.
     * Pas de @ManyToOne — microservice séparé.
     */
    @Column(name = "id_sender")
    private Integer idSender;

    /**
     * ID du destinataire — null pour les abonnements.
     * Utilisé uniquement pour les paiements entre utilisateurs.
     */
    @Column(name = "id_receiver")
    private Integer idReceiver;

    /** Date du paiement — automatique à la création */
    @Builder.Default
    @Column(name = "date_send")
    private LocalDateTime dateSend = LocalDateTime.now();

    /**
     * Méthode de paiement NotchPay.
     * Exemples : "MTN_MONEY", "ORANGE_MONEY", "CARD"
     */
    @Column(name = "methode_paiement", length = 50)
    private String methodePaiement;

    /**
     * Raison du paiement.
     * Pour les abonnements : "Abonnement Premium" ou "Abonnement Pro"
     */
    @Column(name = "raison", length = 255)
    private String raison;

    /**
     * Statut du paiement.
     * EN_ATTENTE par défaut à la création.
     */
    @Builder.Default
    @Column(length = 50)
    private String statut = "EN_ATTENTE";

    /**
     * Lien vers le colis — null pour les abonnements.
     * Utilisé uniquement pour les paiements de livraison.
     */
    @Column(name = "id_c")
    private Integer idColis;

    /**
     * Lien vers l'abonnement activé après confirmation.
     * null tant que le paiement n'est pas COMPLETE.
     * Rempli automatiquement par le webhook NotchPay.
     */
    @Column(name = "id_a")
    private Integer idAbonnement;
}
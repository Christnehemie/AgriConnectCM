package com.agriconnect.service_utilisateurs.service;

import com.agriconnect.service_utilisateurs.model.Utilisateur;
import lombok.Data;

// ── Register Request ──────────────────────────────────
@Data
public class RegisterRequest {
    private String nom;
    private String prenom;
    private String email;
    private String mdp;
    private String numTel;
    private String localHabitat;
    private Utilisateur.Role role;

    // Champs spécifiques selon le rôle
    private String localCult;          // PRODUCTEUR
    private String numCni;             // TRANSPORTEUR
    private String localLivraison;     // ACHETEUR
}

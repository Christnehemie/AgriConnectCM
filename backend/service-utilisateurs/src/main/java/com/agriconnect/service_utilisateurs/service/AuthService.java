package com.agriconnect.service_utilisateurs.service;

import com.agriconnect.service_utilisateurs.model.*;
import com.agriconnect.service_utilisateurs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepo;
    private final ProducteurRepository producteurRepo;
    private final TransporteurRepository transporteurRepo;
    private final AcheteurRepository acheteurRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ── REGISTER ─────────────────────────────────────
    @Transactional
    public Map<String, Object> register(RegisterRequest req) {

        if (utilisateurRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // 1. Créer l'utilisateur de base
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(req.getNom())
                .prenom(req.getPrenom())
                .email(req.getEmail())
                .mdp(passwordEncoder.encode(req.getMdp()))
                .numTel(req.getNumTel())
                .localHabitat(req.getLocalHabitat())
                .role(req.getRole())
                .actif(true)
                .dateInscript(LocalDateTime.now())
                .build();

        utilisateur = utilisateurRepo.save(utilisateur);

        // 2. Créer le profil selon la catégorie/rôle
        switch (req.getRole()) {
            case PRODUCTEUR -> {
                Producteur p = Producteur.builder()
                        .utilisateur(utilisateur)
                        .localCult(req.getLocalCult())
                        .certificationBio(false)
                        .build();
                producteurRepo.save(p);
            }
            case TRANSPORTEUR -> {
                if (req.getNumCni() == null || req.getNumCni().isBlank()) {
                    throw new RuntimeException("Le numéro CNI est obligatoire pour un transporteur");
                }
                Transporteur t = Transporteur.builder()
                        .utilisateur(utilisateur)
                        .numCni(req.getNumCni())
                        .dispoImmediate(true)
                        .build();
                transporteurRepo.save(t);
            }
            case ACHETEUR -> {
                Acheteur a = Acheteur.builder()
                        .utilisateur(utilisateur)
                        .localLivraison(req.getLocalLivraison())
                        .build();
                acheteurRepo.save(a);
            }
        }

        // 3. Générer le token avec l'id ← FIX
        String token = jwtService.generateToken(
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.getId()   // ← id ajouté
        );

        return buildResponse(utilisateur, token);
    }

    // ── LOGIN ─────────────────────────────────────────
    public Map<String, Object> login(String email, String mdp) {

        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!utilisateur.getActif()) {
            throw new RuntimeException("Compte désactivé");
        }

        if (!passwordEncoder.matches(mdp, utilisateur.getMdp())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // Mettre à jour la dernière connexion
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepo.save(utilisateur);

        // Générer le token avec l'id ← FIX
        String token = jwtService.generateToken(
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.getId()   // ← id ajouté
        );

        return buildResponse(utilisateur, token);
    }

    // ── Helper ────────────────────────────────────────
    private Map<String, Object> buildResponse(Utilisateur u, String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role",  u.getRole().name());
        response.put("id",    u.getId());
        response.put("nom",   u.getNom());
        response.put("prenom",u.getPrenom());
        response.put("email", u.getEmail());
        response.put("photo", u.getPhoto());
        return response;
    }
}
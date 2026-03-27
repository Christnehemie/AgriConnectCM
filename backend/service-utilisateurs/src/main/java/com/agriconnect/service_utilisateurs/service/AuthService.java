package com.agriconnect.service_utilisateurs.service;

import com.agriconnect.service_utilisateurs.dto.CoordinatesDTO;
import com.agriconnect.service_utilisateurs.model.*;
import com.agriconnect.service_utilisateurs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepo;
    private final ProducteurRepository producteurRepo;
    private final TransporteurRepository transporteurRepo;
    private final AcheteurRepository acheteurRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AddressParserService addressParserService;  // ✅ AJOUTÉ
    private final GeocodingService geocodingService;          // ✅ AJOUTÉ

    // ── REGISTER AVEC GÉOCODIFICATION AUTOMATIQUE ─────────────────
    @Transactional
    public Map<String, Object> register(RegisterRequest req) {

        if (utilisateurRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // ✅ GÉOCODIFICATION - Convertir l'adresse en coordonnées GPS
        CoordinatesDTO coordinates = null;
        String formattedAddress = req.getLocalHabitat();
        
        if (req.getLocalHabitat() != null && !req.getLocalHabitat().isBlank()) {
            // Essayer de géocoder l'adresse
            coordinates = addressParserService.parseAndGeocode(
                req.getLocalHabitat(),
                null,  // ville
                "Cameroun"
            );
            
            if (coordinates.isSuccess()) {
                formattedAddress = coordinates.getFormattedAddress();
                log.info("📍 Géocodification réussie: {} → ({}, {})", 
                    req.getLocalHabitat(), 
                    coordinates.getLatitude(), 
                    coordinates.getLongitude());
            } else {
                log.warn("⚠️ Échec géocodification pour: {}, erreur: {}", 
                    req.getLocalHabitat(), 
                    coordinates.getErrorMessage());
            }
        }

        // 1. Créer l'utilisateur de base avec les coordonnées
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(req.getNom())
                .prenom(req.getPrenom())
                .email(req.getEmail())
                .mdp(passwordEncoder.encode(req.getMdp()))
                .numTel(req.getNumTel())
                .localHabitat(formattedAddress)  // Adresse formatée
                .role(req.getRole())
                .actif(true)
                .dateInscript(LocalDateTime.now())
                // ✅ AJOUT DES COORDONNÉES GPS
                .latitude(coordinates != null && coordinates.isSuccess() ? 
                    coordinates.getLatitude() : null)
                .longitude(coordinates != null && coordinates.isSuccess() ? 
                    coordinates.getLongitude() : null)
                .build();

        utilisateur = utilisateurRepo.save(utilisateur);
        log.info("👤 Utilisateur créé: {} {} (ID: {})", 
            utilisateur.getPrenom(), utilisateur.getNom(), utilisateur.getId());

        // 2. Créer le profil selon la catégorie/rôle
        switch (req.getRole()) {
            case PRODUCTEUR -> {
                Producteur p = Producteur.builder()
                        .utilisateur(utilisateur)
                        .localCult(req.getLocalCult() != null ? req.getLocalCult() : req.getLocalHabitat())
                        .certificationBio(false)
                        .build();
                producteurRepo.save(p);
                log.info("🌾 Profil producteur créé");
            }
            case TRANSPORTEUR -> {
                if (req.getNumCni() == null || req.getNumCni().isBlank()) {
                    throw new RuntimeException("Le numéro CNI est obligatoire pour un transporteur");
                }
                Transporteur t = Transporteur.builder()
                        .utilisateur(utilisateur)
                        .numCni(req.getNumCni())
                        .dispoImmediate(true)
                        .zoneIntervention(req.getLocalHabitat())  // Zone par défaut
                        .build();
                transporteurRepo.save(t);
                log.info("🚚 Profil transporteur créé");
            }
            case ACHETEUR -> {
                Acheteur a = Acheteur.builder()
                        .utilisateur(utilisateur)
                        .localLivraison(req.getLocalLivraison() != null ? 
                            req.getLocalLivraison() : req.getLocalHabitat())
                        .build();
                acheteurRepo.save(a);
                log.info("🛒 Profil acheteur créé");
            }
        }

        // 3. Générer le token avec l'id
        String token = jwtService.generateToken(
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.getId()
        );

        return buildResponse(utilisateur, token);
    }

    // ── LOGIN (inchangé) ─────────────────────────────────────────
    public Map<String, Object> login(String email, String mdp) {

        Utilisateur utilisateur = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!utilisateur.getActif()) {
            throw new RuntimeException("Compte désactivé");
        }

        if (!passwordEncoder.matches(mdp, utilisateur.getMdp())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepo.save(utilisateur);

        String token = jwtService.generateToken(
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.getId()
        );

        return buildResponse(utilisateur, token);
    }
    
    // ✅ NOUVEAU - Mettre à jour la localisation d'un utilisateur
    @Transactional
    public Map<String, Object> updateLocalisation(Integer userId, String address) {
        Utilisateur utilisateur = utilisateurRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        CoordinatesDTO coordinates = addressParserService.parseAndGeocode(address, null, "Cameroun");
        
        if (coordinates.isSuccess()) {
            utilisateur.setLocalHabitat(coordinates.getFormattedAddress());
            utilisateur.setLatitude(coordinates.getLatitude());
            utilisateur.setLongitude(coordinates.getLongitude());
            utilisateurRepo.save(utilisateur);
            
            // Mettre à jour aussi dans le profil spécifique
            switch (utilisateur.getRole()) {
                case PRODUCTEUR -> producteurRepo.findByUtilisateur(utilisateur).ifPresent(p -> {
                    p.setLocalCult(coordinates.getFormattedAddress());
                    producteurRepo.save(p);
                });
                case TRANSPORTEUR -> transporteurRepo.findByUtilisateur(utilisateur).ifPresent(t -> {
                    t.setZoneIntervention(coordinates.getFormattedAddress());
                    transporteurRepo.save(t);
                });
                case ACHETEUR -> acheteurRepo.findByUtilisateur(utilisateur).ifPresent(a -> {
                    a.setLocalLivraison(coordinates.getFormattedAddress());
                    acheteurRepo.save(a);
                });
            }
            
            log.info("📍 Localisation mise à jour pour l'utilisateur {}: {} → ({}, {})", 
                userId, address, coordinates.getLatitude(), coordinates.getLongitude());
                
            return Map.of(
                "success", true,
                "message", "Localisation mise à jour",
                "latitude", coordinates.getLatitude(),
                "longitude", coordinates.getLongitude(),
                "formattedAddress", coordinates.getFormattedAddress()
            );
        } else {
            return Map.of(
                "success", false,
                "message", "Impossible de géocoder l'adresse: " + coordinates.getErrorMessage()
            );
        }
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
        // ✅ AJOUT des coordonnées GPS dans la réponse
        response.put("latitude", u.getLatitude());
        response.put("longitude", u.getLongitude());
        response.put("localHabitat", u.getLocalHabitat());
        return response;
    }
}
package com.agriconnect.service_utilisateurs.service;

import com.agriconnect.service_utilisateurs.model.*;
import com.agriconnect.service_utilisateurs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UtilisateurRepository  utilisateurRepo;
    private final ProducteurRepository   producteurRepo;
    private final TransporteurRepository transporteurRepo;
    private final AcheteurRepository     acheteurRepo;
    private final PasswordEncoder        passwordEncoder;

    // ── GET PROFIL ────────────────────────────────────────────

    public Map<String, Object> getProfil(String email) {
        Utilisateur u = getByEmail(email);

        Map<String, Object> profil = new HashMap<>();
        profil.put("id",           u.getId());
        profil.put("nom",          u.getNom());
        profil.put("prenom",       u.getPrenom());
        profil.put("email",        u.getEmail());
        profil.put("numTel",       u.getNumTel());
        profil.put("localHabitat", u.getLocalHabitat());
        profil.put("photo",        u.getPhoto());
        profil.put("role",         u.getRole().name());
        profil.put("dateInscript", u.getDateInscript());

        switch (u.getRole()) {
            case PRODUCTEUR -> producteurRepo.findByUtilisateur(u).ifPresent(p -> {
                profil.put("localCult",        p.getLocalCult());
                profil.put("certificationBio", p.getCertificationBio());
            });
            case TRANSPORTEUR -> transporteurRepo.findByUtilisateur(u).ifPresent(t -> {
                profil.put("numCni",         t.getNumCni());
                profil.put("dispoImmediate", t.getDispoImmediate());
            });
            case ACHETEUR -> acheteurRepo.findByUtilisateur(u).ifPresent(a -> {
                profil.put("localLivraison", a.getLocalLivraison());
            });
        }

        return profil;
    }

    // ── UPDATE PROFIL ─────────────────────────────────────────

    @Transactional
    public Map<String, Object> updateProfil(String email, Map<String, Object> body) {
        Utilisateur u = getByEmail(email);

        if (body.get("nom")          != null) u.setNom((String) body.get("nom"));
        if (body.get("prenom")       != null) u.setPrenom((String) body.get("prenom"));
        if (body.get("numTel")       != null) u.setNumTel((String) body.get("numTel"));
        if (body.get("localHabitat") != null) u.setLocalHabitat((String) body.get("localHabitat"));

        utilisateurRepo.save(u);

        switch (u.getRole()) {
            case PRODUCTEUR -> producteurRepo.findByUtilisateur(u).ifPresent(p -> {
                if (body.get("localCult")        != null) p.setLocalCult((String) body.get("localCult"));
                if (body.get("certificationBio") != null) p.setCertificationBio((Boolean) body.get("certificationBio"));
                producteurRepo.save(p);
            });
            case TRANSPORTEUR -> transporteurRepo.findByUtilisateur(u).ifPresent(t -> {
                if (body.get("dispoImmediate") != null) t.setDispoImmediate((Boolean) body.get("dispoImmediate"));
                transporteurRepo.save(t);
            });
            case ACHETEUR -> acheteurRepo.findByUtilisateur(u).ifPresent(a -> {
                if (body.get("localLivraison") != null) a.setLocalLivraison((String) body.get("localLivraison"));
                acheteurRepo.save(a);
            });
        }

        return getProfil(email);
    }

    // ── UPDATE PHOTO ──────────────────────────────────────────

    @Transactional
    public Map<String, Object> updatePhoto(String email, String photoUrl) {
        Utilisateur u = getByEmail(email);
        u.setPhoto(photoUrl);
        utilisateurRepo.save(u);
        return getProfil(email);
    }

    // ── UPDATE PASSWORD ───────────────────────────────────────

    @Transactional
    public void updatePassword(String email, String ancienMdp, String nouveauMdp) {
        Utilisateur u = getByEmail(email);

        if (!passwordEncoder.matches(ancienMdp, u.getMdp()))
            throw new RuntimeException("Ancien mot de passe incorrect");

        if (passwordEncoder.matches(nouveauMdp, u.getMdp()))
            throw new RuntimeException("Le nouveau mot de passe doit être différent de l'ancien");

        u.setMdp(passwordEncoder.encode(nouveauMdp));
        utilisateurRepo.save(u);
    }

    // ── ✅ NOUVEAU — LISTE POUR MESSAGERIE ────────────────────

    /**
     * Retourne tous les utilisateurs actifs sauf l'utilisateur connecté.
     * Utilisé par la messagerie pour afficher la liste des contacts.
     */
    public List<Map<String, Object>> getListe(String emailConnecte) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActif())
                          && !u.getEmail().equals(emailConnecte))
                .map(this::buildPublic)
                .collect(Collectors.toList());
    }

    // ── ✅ NOUVEAU — INFOS PUBLIQUES PAR ID (pour Feign) ──────

    /**
     * Retourne les infos publiques d'un utilisateur par son ID.
     * Appelé par le service-messagerie via Feign pour enrichir
     * les conversations avec nom + photo de l'interlocuteur.
     */
    public Map<String, Object> getPublic(Integer id) {
        Utilisateur u = utilisateurRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + id));
        return buildPublic(u);
    }

    // ── HELPERS ───────────────────────────────────────────────

    private Utilisateur getByEmail(String email) {
        return utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    /**
     * Infos minimales exposées pour la messagerie.
     * Pas de données sensibles (mdp, email, etc.)
     */
    private Map<String, Object> buildPublic(Utilisateur u) {
        Map<String, Object> res = new HashMap<>();
        res.put("id",    u.getId());
        res.put("nom",   u.getNom() + " " + u.getPrenom());
        res.put("photo", u.getPhoto());
        res.put("role",  u.getRole().name());
        return res;
    }
}
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
        // ✅ AJOUT des coordonnées GPS
        profil.put("latitude",     u.getLatitude());
        profil.put("longitude",    u.getLongitude());

        switch (u.getRole()) {
            case PRODUCTEUR -> producteurRepo.findByUtilisateur(u).ifPresent(p -> {
                profil.put("localCult",        p.getLocalCult());
                profil.put("certificationBio", p.getCertificationBio());
                profil.put("photoExploitation", p.getPhotoExploitation());
                profil.put("logoEntreprise", p.getLogoEntreprise());
                profil.put("siteWeb", p.getSiteWeb());
                profil.put("descriptionActivite", p.getDescriptionActivite());
            });
            case TRANSPORTEUR -> transporteurRepo.findByUtilisateur(u).ifPresent(t -> {
                profil.put("numCni",         t.getNumCni());
                profil.put("dispoImmediate", t.getDispoImmediate());
                profil.put("permis", t.getPermis());
                profil.put("zoneIntervention", t.getZoneIntervention());
                profil.put("noteMoyenne", t.getNoteMoyenne());
                profil.put("nbEvaluations", t.getNbEvaluations());
                profil.put("experienceAnnees", t.getExperienceAnnees());
            });
            case ACHETEUR -> acheteurRepo.findByUtilisateur(u).ifPresent(a -> {
                profil.put("localLivraison", a.getLocalLivraison());
                profil.put("modePaiementPrefere", a.getModePaiementPrefere());
            });
        }

        return profil;
    }

    // ✅ NOUVEAU - Récupérer l'ID par email
    public Integer getUserIdByEmail(String email) {
        Utilisateur u = getByEmail(email);
        return u.getId();
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
                if (body.get("photoExploitation") != null) p.setPhotoExploitation((String) body.get("photoExploitation"));
                if (body.get("logoEntreprise")   != null) p.setLogoEntreprise((String) body.get("logoEntreprise"));
                if (body.get("siteWeb")          != null) p.setSiteWeb((String) body.get("siteWeb"));
                if (body.get("descriptionActivite") != null) p.setDescriptionActivite((String) body.get("descriptionActivite"));
                producteurRepo.save(p);
            });
            case TRANSPORTEUR -> transporteurRepo.findByUtilisateur(u).ifPresent(t -> {
                if (body.get("dispoImmediate") != null) t.setDispoImmediate((Boolean) body.get("dispoImmediate"));
                if (body.get("zoneIntervention") != null) t.setZoneIntervention((String) body.get("zoneIntervention"));
                transporteurRepo.save(t);
            });
            case ACHETEUR -> acheteurRepo.findByUtilisateur(u).ifPresent(a -> {
                if (body.get("localLivraison") != null) a.setLocalLivraison((String) body.get("localLivraison"));
                if (body.get("modePaiementPrefere") != null) a.setModePaiementPrefere((String) body.get("modePaiementPrefere"));
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

    // ── LISTE POUR MESSAGERIE ─────────────────────────────────

    public List<Map<String, Object>> getListe(String emailConnecte) {
        return utilisateurRepo.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActif())
                          && !u.getEmail().equals(emailConnecte))
                .map(this::buildPublic)
                .collect(Collectors.toList());
    }

    // ── NOUVEAU - TOUS LES TRANSPORTEURS AVEC LOCATION ─────
    public List<Map<String, Object>> getTransportersWithLocation() {
        return utilisateurRepo.findByRoleAndActifTrueAndLatitudeIsNotNullAndLongitudeIsNotNull(Utilisateur.Role.TRANSPORTEUR)
                .stream()
                .map(this::buildPublic)
                .collect(Collectors.toList());
    }

    // ── INFOS PUBLIQUES PAR ID ────────────────────────────────

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

    private Map<String, Object> buildPublic(Utilisateur u) {
        Map<String, Object> res = new HashMap<>();
        res.put("id",    u.getId());
        res.put("nom",   u.getPrenom() + " " + u.getNom());
        res.put("photo", u.getPhoto());
        res.put("role",  u.getRole().name());
        // ✅ AJOUT des coordonnées GPS dans les infos publiques
        res.put("latitude", u.getLatitude());
        res.put("longitude", u.getLongitude());
        res.put("localHabitat", u.getLocalHabitat());
        return res;
    }
}
package com.agriconnect.service_utilisateurs.service;

import com.agriconnect.service_utilisateurs.model.*;
import com.agriconnect.service_utilisateurs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * UserService — Logique métier de gestion du profil
 * ============================================================
 * Gère :
 *   - Récupération du profil complet selon le rôle
 *   - Modification des informations personnelles
 *   - Mise à jour de la photo (URL Cloudinary)
 *   - Changement de mot de passe
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UtilisateurRepository  utilisateurRepo;
    private final ProducteurRepository   producteurRepo;
    private final TransporteurRepository transporteurRepo;
    private final AcheteurRepository     acheteurRepo;
    private final PasswordEncoder        passwordEncoder;

    // ── GET PROFIL ────────────────────────────────────────────

    /**
     * Récupère le profil complet de l'utilisateur.
     * Retourne les données communes + données spécifiques au rôle.
     *
     * @param email extrait du token JWT par JwtAuthFilter
     */
    public Map<String, Object> getProfil(String email) {
        Utilisateur u = getByEmail(email);

        // Données communes à tous les rôles
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

        // Données spécifiques selon le rôle
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

    /**
     * Met à jour les informations du profil.
     * Seuls les champs fournis (non-null) sont modifiés.
     *
     * @param email extrait du token JWT
     * @param body  map des champs à modifier
     */
    @Transactional
    public Map<String, Object> updateProfil(String email, Map<String, Object> body) {
        Utilisateur u = getByEmail(email);

        // Mettre à jour les champs communs si fournis
        if (body.get("nom")          != null) u.setNom((String) body.get("nom"));
        if (body.get("prenom")       != null) u.setPrenom((String) body.get("prenom"));
        if (body.get("numTel")       != null) u.setNumTel((String) body.get("numTel"));
        if (body.get("localHabitat") != null) u.setLocalHabitat((String) body.get("localHabitat"));

        utilisateurRepo.save(u);

        // Mettre à jour les champs spécifiques au rôle
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

    /**
     * Sauvegarde l'URL Cloudinary de la photo de profil.
     * L'upload est géré côté frontend — on reçoit juste l'URL.
     *
     * @param email    extrait du token JWT
     * @param photoUrl URL sécurisée retournée par Cloudinary
     */
    @Transactional
    public Map<String, Object> updatePhoto(String email, String photoUrl) {
        Utilisateur u = getByEmail(email);
        u.setPhoto(photoUrl);
        utilisateurRepo.save(u);
        return getProfil(email);
    }

    // ── UPDATE PASSWORD ───────────────────────────────────────

    /**
     * Change le mot de passe après vérification de l'ancien.
     *
     * @param email      extrait du token JWT
     * @param ancienMdp  mot de passe actuel pour confirmation
     * @param nouveauMdp nouveau mot de passe souhaité
     */
    @Transactional
    public void updatePassword(String email, String ancienMdp, String nouveauMdp) {
        Utilisateur u = getByEmail(email);

        // Vérifier que l'ancien mot de passe est correct
        if (!passwordEncoder.matches(ancienMdp, u.getMdp())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        // Vérifier que le nouveau est différent de l'ancien
        if (passwordEncoder.matches(nouveauMdp, u.getMdp())) {
            throw new RuntimeException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Encoder et sauvegarder le nouveau mot de passe
        u.setMdp(passwordEncoder.encode(nouveauMdp));
        utilisateurRepo.save(u);
    }

    // ── HELPER ────────────────────────────────────────────────

    /**
     * Récupère un utilisateur par email ou lance une exception
     */
    private Utilisateur getByEmail(String email) {
        return utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
}
package com.agriconnect.service_abonnements.service;

import com.agriconnect.service_abonnements.model.Abonnement;
import com.agriconnect.service_abonnements.model.CategorieAbonnement;
import com.agriconnect.service_abonnements.model.Paiement;
import com.agriconnect.service_abonnements.repository.AbonnementRepository;
import com.agriconnect.service_abonnements.repository.CategorieAbonnementRepository;
import com.agriconnect.service_abonnements.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * AbonnementService — Logique métier abonnements
 * ============================================================
 * Fonctionnalités :
 *   Gratuit → tout bloqué ❌
 *   Premium → messagerie ✅  annonces ✅  paiement ❌  suivi ❌  support ❌
 *   Pro     → tout débloqué ✅
 * ============================================================
 */
@Service
@RequiredArgsConstructor
public class AbonnementService {

    private static final Logger log = LoggerFactory.getLogger(AbonnementService.class);

    private final AbonnementRepository          abonnementRepo;
    private final CategorieAbonnementRepository categorieRepo;
    private final PaiementRepository            paiementRepo;
    private final NotchPayService               notchPayService;

    // ──────────────────────────────────────────────────────
    // MON ABONNEMENT
    // ──────────────────────────────────────────────────────

    public Map<String, Object> getMonAbonnement(Integer idUtilisateur) {
        verifierExpiration(idUtilisateur);

        Optional<Abonnement> aboOpt = abonnementRepo
                .findByIdUtilisateurAndStatut(idUtilisateur, "ACTIF");

        if (aboOpt.isPresent()) {
            return buildResponse(aboOpt.get(), true);
        }

        Optional<Abonnement> expireOpt = abonnementRepo
                .findByIdUtilisateurAndStatut(idUtilisateur, "EXPIRE");

        if (expireOpt.isPresent()) {
            return buildResponse(expireOpt.get(), false);
        }

        return buildGratuitResponse();
    }

    // ──────────────────────────────────────────────────────
    // INITIER PAIEMENT
    // ──────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> initierPaiement(
            Integer idUtilisateur,
            String  libelle,
            String  email
    ) {
        if (libelle.equalsIgnoreCase("Gratuit")) {
            throw new RuntimeException("Impossible de souscrire à Gratuit");
        }

        Optional<Abonnement> actif = abonnementRepo
                .findByIdUtilisateurAndStatut(idUtilisateur, "ACTIF");

        if (actif.isPresent()) {
            Abonnement abo = actif.get();
            throw new RuntimeException(
                    "Abonnement " + abo.getCategorieAbonnement().getLibelle() +
                    " actif jusqu'au " + abo.getDateFin()
            );
        }

        CategorieAbonnement cat = categorieRepo
                .findByLibelleIgnoreCase(libelle)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + libelle));

        int montant = cat.getPrix() != null ? cat.getPrix().intValue() : 2000;

        String reference = notchPayService.genererReference(idUtilisateur, libelle);

        Paiement paiement = Paiement.builder()
                .reference(reference)
                .montant(BigDecimal.valueOf(montant))
                .idSender(idUtilisateur)
                .idReceiver(null)
                .raison("Abonnement " + cat.getLibelle())
                .methodePaiement("NOTCHPAY")
                .statut("EN_ATTENTE")
                .build();

        paiementRepo.save(paiement);
        log.info("Paiement EN_ATTENTE : reference={} idUtilisateur={} montant={} XAF",
                reference, idUtilisateur, montant);

        Map<String, Object> notchPayResult = notchPayService.initierPaiement(
                email,
                montant,
                "Abonnement " + cat.getLibelle() + " AgriConnect",
                reference
        );

        Map<String, Object> result = new HashMap<>();
        result.put("reference",        reference);
        result.put("authorizationUrl", notchPayResult.get("authorizationUrl"));
        result.put("montant",          montant);
        result.put("categorie",        cat.getLibelle());
        result.put("statut",           "EN_ATTENTE");
        return result;
    }

    // ──────────────────────────────────────────────────────
    // CONFIRMER PAIEMENT (webhook)
    // ──────────────────────────────────────────────────────

    @Transactional
    public void confirmerPaiement(String reference, String statut) {
        Paiement paiement = paiementRepo.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable : " + reference));

        if ("COMPLETE".equals(paiement.getStatut())) {
            log.warn("Paiement déjà traité : reference={}", reference);
            return;
        }

        boolean paiementConfirme = "complete".equalsIgnoreCase(statut) &&
                notchPayService.verifierPaiement(reference);

        if (!paiementConfirme) {
            paiement.setStatut("ECHEC");
            paiementRepo.save(paiement);
            log.warn("Paiement ECHEC : reference={} statut={}", reference, statut);
            return;
        }

        // Extraire infos : abo_{idUtilisateur}_{categorie}_{timestamp}
        String[] parts            = reference.split("_");
        Integer  idUtilisateur    = Integer.parseInt(parts[1]);
        String   libelleCategorie = parts[2];

        CategorieAbonnement cat = categorieRepo
                .findByLibelleIgnoreCase(libelleCategorie)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + libelleCategorie));

        LocalDateTime now   = LocalDateTime.now();
        int           duree = cat.getDuree() != null ? cat.getDuree() : 30;

        Abonnement nouvelAbo = Abonnement.builder()
                .idUtilisateur(idUtilisateur)
                .categorieAbonnement(cat)
                .dateAbo(now)
                .dateFin(now.plusDays(duree))
                .statut("ACTIF")
                .build();

        Abonnement saved = abonnementRepo.save(nouvelAbo);

        paiement.setStatut("COMPLETE");
        paiement.setIdAbonnement(saved.getId());
        paiementRepo.save(paiement);

        log.info("Abonnement activé : idUtilisateur={} categorie={} dateFin={}",
                idUtilisateur, cat.getLibelle(), saved.getDateFin());
    }

    // ──────────────────────────────────────────────────────
    // RENOUVELER
    // ──────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> initierRenouvellement(Integer idUtilisateur, String email) {
        Optional<Abonnement> actif = abonnementRepo
                .findByIdUtilisateurAndStatut(idUtilisateur, "ACTIF");

        if (actif.isPresent()) {
            throw new RuntimeException(
                    "Abonnement encore actif jusqu'au " + actif.get().getDateFin()
            );
        }

        Optional<Abonnement> expire = abonnementRepo
                .findByIdUtilisateurAndStatut(idUtilisateur, "EXPIRE");

        if (expire.isEmpty()) {
            throw new RuntimeException("Aucun abonnement expiré à renouveler");
        }

        String libelle = expire.get().getCategorieAbonnement().getLibelle();
        return initierPaiement(idUtilisateur, libelle, email);
    }

    // ──────────────────────────────────────────────────────
    // CATEGORIES
    // ──────────────────────────────────────────────────────

    public List<Map<String, Object>> getCategories() {
        return categorieRepo.findAll().stream()
                .map(cat -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",              cat.getId());
                    m.put("libelle",         cat.getLibelle());
                    m.put("duree",           cat.getDuree());
                    m.put("prix",            cat.getPrix());
                    m.put("fonctionnalites", getFonctionnalites(cat.getLibelle()));
                    return m;
                })
                .toList();
    }

    // ──────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ──────────────────────────────────────────────────────

    private void verifierExpiration(Integer idUtilisateur) {
        abonnementRepo.findByIdUtilisateurAndStatut(idUtilisateur, "ACTIF")
                .ifPresent(abo -> {
                    if (abo.getDateFin() != null &&
                            LocalDateTime.now().isAfter(abo.getDateFin())) {
                        abo.setStatut("EXPIRE");
                        abonnementRepo.save(abo);
                    }
                });
    }

    private Map<String, Object> buildResponse(Abonnement abo, boolean estActif) {
        CategorieAbonnement cat = abo.getCategorieAbonnement();
        Map<String, Object> res = new HashMap<>();
        res.put("idAbonnement",    abo.getId());
        res.put("categorie",       cat.getLibelle());
        res.put("prix",            cat.getPrix());
        res.put("dureeJours",      cat.getDuree());
        res.put("dateDebut",       abo.getDateAbo());
        res.put("dateFin",         abo.getDateFin());
        res.put("statut",          abo.getStatut());
        res.put("estActif",        estActif);
        res.put("estExpire",       !estActif);
        res.put("fonctionnalites", getFonctionnalites(cat.getLibelle()));
        return res;
    }

    private Map<String, Object> buildGratuitResponse() {
        Map<String, Object> res = new HashMap<>();
        res.put("idAbonnement",    null);
        res.put("categorie",       "Gratuit");
        res.put("prix",            0);
        res.put("dureeJours",      null);
        res.put("dateDebut",       null);
        res.put("dateFin",         null);
        res.put("statut",          "GRATUIT");
        res.put("estActif",        false);
        res.put("estExpire",       false);
        res.put("fonctionnalites", getFonctionnalites("Gratuit"));
        return res;
    }

    private Map<String, Boolean> getFonctionnalites(String libelle) {
        Map<String, Boolean> f = new HashMap<>();
        switch (libelle.toLowerCase()) {
            case "premium" -> {
                f.put("messagerie", true);
                f.put("annonces",   true);
                f.put("paiement",   false);
                f.put("suivi",      false);
                f.put("support",    false);
            }
            case "pro" -> {
                f.put("messagerie", true);
                f.put("annonces",   true);
                f.put("paiement",   true);
                f.put("suivi",      true);
                f.put("support",    true);
            }
            default -> {
                f.put("messagerie", false);
                f.put("annonces",   false);
                f.put("paiement",   false);
                f.put("suivi",      false);
                f.put("support",    false);
            }
        }
        return f;
    }
}
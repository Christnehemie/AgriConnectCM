package com.agriconnect.service_abonnements.controller;

import com.agriconnect.service_abonnements.service.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ============================================================
 * AbonnementController — Endpoints gestion abonnements
 * ============================================================
 * GET  /api/abonnements/mon-abonnement       → abonnement actuel
 * POST /api/abonnements/initier-paiement     → initie NotchPay
 * POST /api/abonnements/webhook-notchpay     → confirmation paiement
 * PUT  /api/abonnements/renouveler           → renouveler si expiré
 * GET  /api/abonnements/categories           → liste des offres
 * ============================================================
 */
@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private static final Logger log = LoggerFactory.getLogger(AbonnementController.class);

    private final AbonnementService abonnementService;

    // ──────────────────────────────────────────────────────
    // GET — Mon abonnement actuel
    // ──────────────────────────────────────────────────────

    @GetMapping("/mon-abonnement")
    public ResponseEntity<?> getMonAbonnement(Authentication auth) {
        try {
            Integer idUtilisateur = (Integer) auth.getDetails();
            return ResponseEntity.ok(abonnementService.getMonAbonnement(idUtilisateur));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────
    // POST — Initier un paiement NotchPay
    // ──────────────────────────────────────────────────────

    @PostMapping("/initier-paiement")
    public ResponseEntity<?> initierPaiement(
            Authentication auth,
            @RequestBody Map<String, String> body
    ) {
        try {
            String categorie = body.get("categorie");
            String email     = body.get("email");

            if (categorie == null || categorie.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "categorie obligatoire (Premium ou Pro)"));
            }
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "email obligatoire"));
            }

            Integer idUtilisateur = (Integer) auth.getDetails();
            return ResponseEntity.ok(
                    abonnementService.initierPaiement(idUtilisateur, categorie, email)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────
    // POST — Webhook NotchPay (PUBLIC — pas de JWT)
    // ──────────────────────────────────────────────────────

    @PostMapping("/webhook-notchpay")
    public ResponseEntity<?> webhookNotchPay(@RequestBody Map<String, Object> body) {
        try {
            String reference = (String) body.get("reference");
            String statut    = (String) body.get("status");

            log.info("Webhook NotchPay reçu : reference={} statut={}", reference, statut);

            if (reference == null || statut == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "reference et status obligatoires"));
            }

            abonnementService.confirmerPaiement(reference, statut);
            return ResponseEntity.ok(Map.of("message", "Traité avec succès"));

        } catch (RuntimeException e) {
            log.error("Erreur webhook : {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────
    // PUT — Renouveler l'abonnement expiré
    // ──────────────────────────────────────────────────────

    @PutMapping("/renouveler")
    public ResponseEntity<?> renouveler(
            Authentication auth,
            @RequestBody Map<String, String> body
    ) {
        try {
            String email = body.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "email obligatoire"));
            }
            Integer idUtilisateur = (Integer) auth.getDetails();
            return ResponseEntity.ok(
                    abonnementService.initierRenouvellement(idUtilisateur, email)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────
    // GET — Catégories disponibles
    // ──────────────────────────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            return ResponseEntity.ok(abonnementService.getCategories());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
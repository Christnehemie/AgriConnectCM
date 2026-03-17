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
 * GET  /api/abonnements/mon-abonnement         → abonnement actuel
 * POST /api/abonnements/initier-paiement       → initie NotchPay
 * POST /api/abonnements/webhook-notchpay       → webhook POST NotchPay
 * GET  /api/abonnements/callback-notchpay      → callback GET NotchPay (redirect navigateur)
 * PUT  /api/abonnements/renouveler             → renouveler si expiré
 * GET  /api/abonnements/categories             → liste des offres
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

    /**
     * Body : { "categorie": "Premium" | "Pro", "email": "user@mail.com" }
     * Retourne : { "authorizationUrl": "https://pay.notchpay.co/..." }
     */
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

            // FIX : idUtilisateur depuis le JWT — jamais null
            Integer idUtilisateur = (Integer) auth.getDetails();
            log.info("Initier paiement : idUtilisateur={} categorie={}", idUtilisateur, categorie);

            return ResponseEntity.ok(
                    abonnementService.initierPaiement(idUtilisateur, categorie, email)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────
    // POST — Webhook NotchPay (appelé par NotchPay en POST)
    // ──────────────────────────────────────────────────────

    @PostMapping("/webhook-notchpay")
    public ResponseEntity<?> webhookNotchPay(@RequestBody Map<String, Object> body) {
        try {
            String reference = (String) body.get("reference");
            String statut    = (String) body.get("status");

            log.info("Webhook POST NotchPay : reference={} statut={}", reference, statut);

            if (reference == null || statut == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "reference et status obligatoires"));
            }

            String result = abonnementService.confirmerPaiement(reference, statut);
            return ResponseEntity.ok(Map.of("message", result));

        } catch (RuntimeException e) {
            log.error("Erreur webhook POST : {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────
    // GET — Callback NotchPay (redirect navigateur après paiement)
    // ──────────────────────────────────────────────────────

    /**
     * NotchPay redirige le navigateur vers cette URL après paiement.
     * Paramètres reçus : reference, trxref, notchpay_trxref
     *
     * On active l'abonnement puis on redirige vers l'app.
     */
    @GetMapping("/webhook-notchpay")
    public ResponseEntity<?> callbackNotchPay(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String trxref,
            @RequestParam(name = "notchpay_trxref", required = false) String notchpayTrxref
    ) {
        try {
            // NotchPay peut envoyer la référence dans différents paramètres
            String ref = notchpayTrxref != null ? notchpayTrxref :
                        (trxref        != null ? trxref          : reference);

            log.info("Callback GET NotchPay : reference={} trxref={} notchpay_trxref={}",
                    reference, trxref, notchpayTrxref);
            log.info("Référence utilisée : {}", ref);

            if (ref == null) {
                return ResponseEntity.badRequest().body("Référence manquante");
            }

            String result = abonnementService.confirmerPaiement(ref, "complete");
            log.info("Résultat confirmation : {}", result);

            // Retourner une page HTML simple qui ferme et renvoie à l'app
            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"><title>Paiement AgriConnect</title></head>
                    <body style="font-family:sans-serif;text-align:center;padding:40px;background:#f2f2f7">
                      <div style="background:white;border-radius:16px;padding:32px;max-width:400px;margin:auto;box-shadow:0 4px 20px rgba(0,0,0,.1)">
                        <div style="font-size:60px">✅</div>
                        <h2 style="color:#34c759">Paiement réussi !</h2>
                        <p style="color:#8e8e93">Votre abonnement AgriConnect est activé.</p>
                        <p style="color:#8e8e93;font-size:14px">Vous pouvez fermer cette page et retourner sur l'application.</p>
                        <button onclick="window.close()"
                          style="background:#34c759;color:white;border:none;padding:14px 32px;border-radius:12px;font-size:16px;font-weight:600;cursor:pointer;margin-top:16px">
                          Retour à l'app
                        </button>
                      </div>
                    </body>
                    </html>
                    """;

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html;charset=UTF-8")
                    .body(html);

        } catch (RuntimeException e) {
            log.error("Erreur callback GET : {}", e.getMessage());

            String htmlErreur = """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"><title>Erreur</title></head>
                    <body style="font-family:sans-serif;text-align:center;padding:40px;background:#f2f2f7">
                      <div style="background:white;border-radius:16px;padding:32px;max-width:400px;margin:auto">
                        <div style="font-size:60px">❌</div>
                        <h2 style="color:#ff3b30">Erreur de paiement</h2>
                        <p style="color:#8e8e93">Veuillez réessayer depuis l'application.</p>
                      </div>
                    </body>
                    </html>
                    """;

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html;charset=UTF-8")
                    .body(htmlErreur);
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
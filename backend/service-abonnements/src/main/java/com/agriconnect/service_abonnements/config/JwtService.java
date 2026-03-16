package com.agriconnect.service_abonnements.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ============================================================
 * JwtService — Validation des tokens JWT
 * ============================================================
 * Utilise le même secret que service-utilisateurs.
 * On ne génère pas de tokens ici — on les valide seulement.
 *
 * Le token contient :
 *   - subject : email
 *   - claim "role" : rôle utilisateur
 *   - claim "id"   : id utilisateur (clé pour les abonnements)
 * ============================================================
 */
@Component
public class JwtService {

    /** Même secret que service-utilisateurs */
    @Value("${jwt.secret}")
    private String secret;

    /** Génère la clé HMAC depuis la chaîne secrète */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Extrait tous les claims du token */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extrait l'email depuis le token */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /** Extrait le rôle depuis le token */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Extrait l'id utilisateur depuis le token.
     * Utilisé pour identifier l'utilisateur sans appeler
     * service-utilisateurs.
     */
    public Integer extractUserId(String token) {
        return extractClaims(token).get("id", Integer.class);
    }

    /** Vérifie si le token est encore valide */
    public boolean isValid(String token) {
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
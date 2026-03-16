package com.agriconnect.service_utilisateurs.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Gestion des tokens JWT.
 * - Génération du token (email + rôle + id)
 * - Extraction de l'email
 * - Extraction du rôle
 * - Vérification de validité
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /** Génère la clé HMAC depuis la chaîne secrète */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Génère un token JWT signé avec email + rôle + id.
     * Le claim "id" est nécessaire pour service-abonnements
     * et tous les autres microservices qui identifient
     * l'utilisateur sans appeler service-utilisateurs.
     */
    public String generateToken(String email, String role, Integer id) {
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("role", role, "id", id))  // ← id ajouté ici
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /** Extrait tous les claims du token */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extrait l'email (subject) depuis le token */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Extrait le rôle depuis le claim "role" du token */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /** Vérifie si le token est encore valide */
    public boolean isTokenValid(String token) {
        try {
            return extractAllClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
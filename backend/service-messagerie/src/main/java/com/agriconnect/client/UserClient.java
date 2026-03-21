package com.agriconnect.service_messagerie.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * UserClient — Feign vers service-utilisateur
 * ============================================================
 * Permet au service-messagerie d'enrichir les conversations
 * avec les noms/photos des interlocuteurs.
 * ============================================================
 */
@FeignClient(name = "service-utilisateurs", path = "/api/users")
public interface UserClient {

    /**
     * Liste de tous les utilisateurs actifs (hors connecté).
     * Appelé avec le token JWT de l'utilisateur courant.
     */
    @GetMapping("/liste")
    List<Map<String, Object>> getListe(
            @RequestHeader("Authorization") String bearerToken
    );

    /**
     * Infos publiques d'un utilisateur par son ID.
     */
    @GetMapping("/{id}/public")
    Map<String, Object> getPublic(
            @PathVariable("id") Integer id
    );
}
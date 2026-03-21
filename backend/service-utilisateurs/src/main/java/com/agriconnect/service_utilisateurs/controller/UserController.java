package com.agriconnect.service_utilisateurs.controller;

import com.agriconnect.service_utilisateurs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profil")
    public ResponseEntity<?> getProfil(Authentication auth) {
        try {
            return ResponseEntity.ok(userService.getProfil(auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/profil")
    public ResponseEntity<?> updateProfil(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(userService.updateProfil(auth.getName(), body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/profil/photo")
    public ResponseEntity<?> updatePhoto(Authentication auth, @RequestBody Map<String, String> body) {
        try {
            String photoUrl = body.get("photoUrl");
            if (photoUrl == null || photoUrl.isBlank())
                return ResponseEntity.badRequest().body(Map.of("erreur", "URL photo obligatoire"));
            return ResponseEntity.ok(userService.updatePhoto(auth.getName(), photoUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/profil/password")
    public ResponseEntity<?> updatePassword(Authentication auth, @RequestBody Map<String, String> body) {
        try {
            userService.updatePassword(auth.getName(), body.get("ancienMdp"), body.get("nouveauMdp"));
            return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ✅ NOUVEAU — Liste de tous les utilisateurs actifs (pour la messagerie)
    // GET /api/users/liste
    @GetMapping("/liste")
    public ResponseEntity<?> getListe(Authentication auth) {
        try {
            // Exclure l'utilisateur connecté de la liste
            return ResponseEntity.ok(userService.getListe(auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ✅ NOUVEAU — Infos publiques d'un utilisateur par son ID
    // GET /api/users/{id}/public
    @GetMapping("/{id}/public")
    public ResponseEntity<?> getPublic(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.getPublic(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
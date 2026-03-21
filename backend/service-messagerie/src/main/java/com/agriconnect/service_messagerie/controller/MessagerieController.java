package com.agriconnect.service_messagerie.controller;

import com.agriconnect.service_messagerie.client.UserClient;
import com.agriconnect.service_messagerie.model.Conversation;
import com.agriconnect.service_messagerie.model.Message;
import com.agriconnect.service_messagerie.service.CloudinaryService;
import com.agriconnect.service_messagerie.service.MessagerieService;
import com.agriconnect.service_messagerie.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messagerie")
@RequiredArgsConstructor
public class MessagerieController {

    private static final Logger log = LoggerFactory.getLogger(MessagerieController.class);

    private final MessagerieService     messagerieService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserClient            userClient;
    private final CloudinaryService     cloudinaryService;
    private final PresenceService       presenceService;

    private Integer getIdUser(Authentication auth) {
        return (Integer) auth.getDetails();
    }

    // ── Users ─────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> getUtilisateurs(
            Authentication auth,
            @RequestHeader("Authorization") String token
    ) {
        try {
            List<Map<String, Object>> users = userClient.getListe(token);
            // ✅ Enrichir avec statut en ligne
            users.forEach(u -> {
                Integer id = (Integer) u.get("id");
                u.put("enLigne", presenceService.estEnLigne(id));
            });
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ── Conversations ─────────────────────────────────────

    @GetMapping("/conversations")
    public ResponseEntity<?> getMesConversations(
            Authentication auth,
            @RequestHeader("Authorization") String token
    ) {
        try {
            Integer idUser = getIdUser(auth);
            List<Map<String, Object>> convs = messagerieService.getMesConversationsEnrichies(idUser, token);
            // ✅ Enrichir avec statut en ligne de l'interlocuteur
            convs.forEach(c -> {
                Integer idSender   = (Integer) c.get("idSender");
                Integer idReceiver = (Integer) c.get("idReceiver");
                Integer idInter    = idSender.equals(idUser) ? idReceiver : idSender;
                c.put("enLigne", presenceService.estEnLigne(idInter));
            });
            return ResponseEntity.ok(convs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/conversations")
    public ResponseEntity<?> ouvrirConversation(
            Authentication auth,
            @RequestBody Map<String, Integer> body
    ) {
        try {
            Integer idSender   = getIdUser(auth);
            Integer idReceiver = body.get("idReceiver");
            if (idReceiver == null)
                return ResponseEntity.badRequest().body(Map.of("erreur", "idReceiver obligatoire"));

            Conversation conv = messagerieService.ouvrirConversation(idSender, idReceiver);

            Map<String, Object> res = new HashMap<>();
            res.put("idConversation", conv.getId());
            res.put("idSender",       conv.getIdSender());
            res.put("idReceiver",     conv.getIdReceiver());
            res.put("dateCreation",   conv.getDateCreation());
            res.put("nonLus",         0);
            res.put("epingle",        false);
            res.put("archive",        false);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ✅ Épingler / désépingler
    @PutMapping("/conversations/{id}/epingle")
    public ResponseEntity<?> epingler(
            Authentication auth,
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body
    ) {
        try {
            messagerieService.epinglerConversation(id, getIdUser(auth), Boolean.TRUE.equals(body.get("epingle")));
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ✅ Archiver / désarchiver
    @PutMapping("/conversations/{id}/archive")
    public ResponseEntity<?> archiver(
            Authentication auth,
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body
    ) {
        try {
            messagerieService.archiverConversation(id, getIdUser(auth), Boolean.TRUE.equals(body.get("archive")));
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ✅ Supprimer conversation
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<?> supprimerConversation(
            Authentication auth,
            @PathVariable Integer id
    ) {
        try {
            messagerieService.supprimerConversation(id, getIdUser(auth));
            return ResponseEntity.ok(Map.of("message", "Conversation supprimée"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ── Messages ──────────────────────────────────────────

    @GetMapping("/conversations/{idConversation}/messages")
    public ResponseEntity<?> getMessages(Authentication auth, @PathVariable Integer idConversation) {
        try {
            return ResponseEntity.ok(messagerieService.getMessages(idConversation, getIdUser(auth)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<?> envoyerMessage(Authentication auth, @RequestBody Map<String, Object> body) {
        try {
            Integer idUser         = getIdUser(auth);
            Integer idConversation = (Integer) body.get("idConversation");
            String  contenu        = (String)  body.get("contenu");
            String  pieceJointe    = (String)  body.get("pieceJointe");
            String  typeMessage    = (String)  body.get("typeMessage");
            Integer idReply        = (Integer) body.get("idReply");
            if (idConversation == null)
                return ResponseEntity.badRequest().body(Map.of("erreur", "idConversation obligatoire"));
            Message msg = messagerieService.envoyerMessage(idConversation, idUser, contenu, pieceJointe, typeMessage, idReply);
            messagingTemplate.convertAndSend("/topic/conversation." + idConversation, msg);
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/messages/media")
    public ResponseEntity<?> envoyerMedia(
            Authentication auth,
            @RequestParam("file")           MultipartFile file,
            @RequestParam("idConversation") Integer idConversation,
            @RequestParam(value = "idReply", required = false) Integer idReply
    ) {
        try {
            Integer idUser = getIdUser(auth);
            if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier vide"));
            Map<String, String> upload = cloudinaryService.upload(file);
            Message msg = messagerieService.envoyerMessage(
                idConversation, idUser, null, upload.get("url"), upload.get("typeMessage"), idReply
            );
            messagingTemplate.convertAndSend("/topic/conversation." + idConversation, msg);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/conversations/{idConversation}/lu")
    public ResponseEntity<?> marquerLus(Authentication auth, @PathVariable Integer idConversation) {
        try {
            Integer idUser = getIdUser(auth);
            messagerieService.marquerLus(idConversation, idUser);
            messagingTemplate.convertAndSend(
                "/topic/conversation." + idConversation + ".lu",
                Map.of("idConversation", idConversation, "idUser", idUser, "lu", true)
            );
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/messages/{idMessage}")
    public ResponseEntity<?> supprimerMessage(
            Authentication auth,
            @PathVariable Integer idMessage,
            @RequestParam(defaultValue = "false") boolean pourTous
    ) {
        try {
            Map<String, Object> result = messagerieService.supprimerMessage(idMessage, getIdUser(auth), pourTous);
            if (pourTous) messagingTemplate.convertAndSend("/topic/message.supprime", result);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/messages/{idMessage}/reaction")
    public ResponseEntity<?> reagir(
            Authentication auth,
            @PathVariable Integer idMessage,
            @RequestBody Map<String, String> body
    ) {
        try {
            String emoji = body.get("emoji");
            if (emoji == null || emoji.isBlank())
                return ResponseEntity.badRequest().body(Map.of("erreur", "emoji obligatoire"));
            Map<String, Object> result = messagerieService.reagir(idMessage, getIdUser(auth), emoji);
            messagingTemplate.convertAndSend("/topic/message.reaction", result);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
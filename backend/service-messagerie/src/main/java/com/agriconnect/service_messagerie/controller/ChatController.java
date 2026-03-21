package com.agriconnect.service_messagerie.controller;

import com.agriconnect.service_messagerie.model.Message;
import com.agriconnect.service_messagerie.service.MessagerieService;
import com.agriconnect.service_messagerie.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * Place ce fichier dans :
 * src/main/java/com/agriconnect/service_messagerie/controller/ChatController.java
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final MessagerieService     messagerieService;
    private final PresenceService       presenceService;

    @MessageMapping("/message.envoyer")
    public void envoyerMessage(@Payload Map<String, Object> payload) {
        try {
            Integer idConversation = (Integer) payload.get("idConversation");
            Integer idSender       = (Integer) payload.get("idSender");
            String  contenu        = (String)  payload.get("contenu");
            String  pieceJointe    = (String)  payload.get("pieceJointe");
            String  typeMessage    = (String)  payload.get("typeMessage");
            Integer idReply        = (Integer) payload.get("idReply");

            Message msg = messagerieService.envoyerMessage(
                idConversation, idSender, contenu, pieceJointe, typeMessage, idReply
            );
            presenceService.mettreAJour(idSender);
            messagingTemplate.convertAndSend("/topic/conversation." + idConversation, msg);
            log.info("WS message : conv={} sender={}", idConversation, idSender);
        } catch (Exception e) {
            log.error("Erreur envoyerMessage : {}", e.getMessage());
        }
    }

    @MessageMapping("/message.frappe")
    public void indicateurFrappe(@Payload Map<String, Object> payload) {
        Integer idConversation = (Integer) payload.get("idConversation");
        messagingTemplate.convertAndSend("/topic/frappe." + idConversation, payload);
    }

    @MessageMapping("/message.lu")
    public void marquerLu(@Payload Map<String, Object> payload) {
        try {
            Integer idConversation = (Integer) payload.get("idConversation");
            Integer idUser         = (Integer) payload.get("idUser");
            messagerieService.marquerLus(idConversation, idUser);
            messagingTemplate.convertAndSend(
                "/topic/conversation." + idConversation + ".lu",
                Map.of("idConversation", idConversation, "idUser", idUser, "lu", true)
            );
        } catch (Exception e) {
            log.error("Erreur marquerLu : {}", e.getMessage());
        }
    }

    @MessageMapping("/presence")
    public void presence(
        @Payload Map<String, Object> payload,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        Integer idUser   = (Integer) payload.get("idUser");
        Boolean enLigne  = (Boolean) payload.get("enLigne");
        String  session  = headerAccessor.getSessionId();

        if (Boolean.TRUE.equals(enLigne)) presenceService.connecter(idUser, session);
        else presenceService.deconnecter(session);

        messagingTemplate.convertAndSend(
            "/topic/presence." + idUser,
            Map.of("idUser", idUser, "enLigne", enLigne)
        );
        log.info("Présence : idUser={} enLigne={}", idUser, enLigne);
    }

    @MessageMapping("/presence.check")
    public void checkPresence(@Payload Map<String, Object> payload) {
        Integer idUserCible = (Integer) payload.get("idUserCible");
        messagingTemplate.convertAndSend(
            "/topic/presence." + idUserCible,
            Map.of("idUser", idUserCible, "enLigne", presenceService.estEnLigne(idUserCible))
        );
    }
}
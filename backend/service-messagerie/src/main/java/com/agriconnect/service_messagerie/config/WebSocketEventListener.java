package com.agriconnect.service_messagerie.config;

import com.agriconnect.service_messagerie.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

/**
 * Détecte connexion/déconnexion WebSocket automatiquement.
 * Place ce fichier dans :
 * src/main/java/com/agriconnect/service_messagerie/config/WebSocketEventListener.java
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final PresenceService       presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("WS connecté : session={}", accessor.getSessionId());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor  = StompHeaderAccessor.wrap(event.getMessage());
        String              sessionId = accessor.getSessionId();
        Integer             idUser    = presenceService.deconnecter(sessionId);

        if (idUser != null) {
            log.info("WS déconnecté : idUser={}", idUser);
            messagingTemplate.convertAndSend(
                "/topic/presence." + idUser,
                Map.of("idUser", idUser, "enLigne", false)
            );
        }
    }
}
package com.agriconnect.service_messagerie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket STOMP
 *
 * Endpoints :
 *   /ws                    → point de connexion WebSocket
 *
 * Topics (subscribe) :
 *   /topic/conversation.{id}  → messages d'une conversation
 *   /topic/frappe.{id}        → indicateur de frappe
 *   /user/{id}/queue/messages → messages privés directs
 *
 * App destinations (send) :
 *   /app/message.envoyer      → envoyer un message
 *   /app/message.frappe       → indicateur de frappe
 *   /app/message.lu           → marquer comme lu
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker simple en mémoire
        registry.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les destinations gérées par @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        // Préfixe pour les messages utilisateur
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // fallback pour les navigateurs sans WebSocket natif
    }
}
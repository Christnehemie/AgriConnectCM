package com.agriconnect.service_offre.controller;

import com.agriconnect.service_offre.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/location/update")
    public void processLocationUpdate(@Payload LocationMessage message) {
        log.info("📍 Réception position pour user #{} : lat={}, lon={}", 
                 message.getIdUtilisateur(), message.getLatitude(), message.getLongitude());
                 
        if (message.getLastUpdated() == null) {
            message.setLastUpdated(LocalDateTime.now());
        }
        
        // Broadcast la position à tous ceux qui écoutent ce topic
        messagingTemplate.convertAndSend("/topic/locations", message);
    }
}

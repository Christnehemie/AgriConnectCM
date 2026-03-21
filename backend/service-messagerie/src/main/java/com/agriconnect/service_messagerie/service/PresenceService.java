package com.agriconnect.service_messagerie.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Présence en ligne — stockage en mémoire.
 * Place ce fichier dans :
 * src/main/java/com/agriconnect/service_messagerie/service/PresenceService.java
 */
@Service
public class PresenceService {

    private final Map<Integer, LocalDateTime> enligne       = new ConcurrentHashMap<>();
    private final Map<String, Integer>        sessionToUser = new ConcurrentHashMap<>();

    public void connecter(Integer idUser, String sessionId) {
        enligne.put(idUser, LocalDateTime.now());
        sessionToUser.put(sessionId, idUser);
    }

    public Integer deconnecter(String sessionId) {
        Integer idUser = sessionToUser.remove(sessionId);
        if (idUser != null) enligne.remove(idUser);
        return idUser;
    }

    public void mettreAJour(Integer idUser) {
        if (enligne.containsKey(idUser)) enligne.put(idUser, LocalDateTime.now());
    }

    public boolean estEnLigne(Integer idUser) {
        return enligne.containsKey(idUser);
    }

    public LocalDateTime getDerniereActivite(Integer idUser) {
        return enligne.get(idUser);
    }

    public Set<Integer> getTousEnLigne() {
        return enligne.keySet();
    }
}
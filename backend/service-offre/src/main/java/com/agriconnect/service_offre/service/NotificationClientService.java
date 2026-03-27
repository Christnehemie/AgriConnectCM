package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.dto.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationClientService {

    private final RestTemplate restTemplate;
    
    @Value("${service.notification.url:http://localhost:8085}")
    private String notificationServiceUrl;
    
    @Value("${service.notification.enabled:false}")
    private boolean notificationEnabled;

    /**
     * Envoie une notification (log uniquement si service notification désactivé)
     */
    public boolean envoyerNotification(NotificationRequestDTO notification) {
        if (!notificationEnabled) {
            log.info("🔔 [SIMULATION] Notification pour utilisateur {}: {} - {}", 
                notification.getIdUtilisateur(), 
                notification.getTitre(), 
                notification.getContenu());
            return true;
        }
        
        try {
            String url = notificationServiceUrl + "/api/notifications/envoyer";
            restTemplate.postForObject(url, notification, Void.class);
            log.info("📨 Notification envoyée à l'utilisateur {}", notification.getIdUtilisateur());
            return true;
        } catch (Exception e) {
            log.warn("❌ Service notification indisponible: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Construit et envoie une notification de nouvelle mission
     */
    public boolean notifierNouvelleMission(Integer idUtilisateur,
                                            Integer idCommande,
                                            String nomProduit,
                                            Integer quantite,
                                            String uniteMesure,
                                            BigDecimal prixTotal,
                                            Double distanceKm) {
        
        String message = String.format(
            "📦 Nouvelle mission !\n\n" +
            "Commande #%d\n" +
            "Produit: %s\n" +
            "Quantité: %d %s\n" +
            "Montant: %,.0f FCFA\n" +
            "Distance: %.1f km du producteur\n\n" +
            "Acceptez-vous cette mission ?",
            idCommande, 
            nomProduit != null ? nomProduit : "produit", 
            quantite, 
            uniteMesure != null ? uniteMesure : "kg", 
            prixTotal, 
            distanceKm != null ? distanceKm : 0
        );
        
        NotificationRequestDTO request = NotificationRequestDTO.builder()
            .idUtilisateur(idUtilisateur)
            .titre("🚚 Nouvelle mission de livraison")
            .contenu(message)
            .type("NOUVELLE_MISSION")
            .idCommande(idCommande)
            .lienAction("/tabs/missions?commande=" + idCommande)
            .nomProduit(nomProduit)
            .quantite(quantite)
            .uniteMesure(uniteMesure)
            .prixTotal(prixTotal)
            .distanceKm(distanceKm)
            .build();
        
        return envoyerNotification(request);
    }
}
package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.model.Transporteur;
import com.agriconnect.service_offre.model.Utilisateur;
import com.agriconnect.service_offre.repository.TransporteurRepository;
import com.agriconnect.service_offre.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgoTransporteurService {

    private final TransporteurRepository transporteurRepo;
    private final UtilisateurRepository utilisateurRepo;
    private final NotificationClientService notificationClientService;

    /**
     * Trouve le transporteur le plus proche du producteur
     * Calcule la distance avec la formule de Haversine
     */
    public TransporteurInfo trouverTransporteurLePlusProche(BigDecimal latProducteur, 
                                                             BigDecimal lonProducteur,
                                                             BigDecimal poidsKg) {
        
        log.info("🔍 Recherche transporteur le plus proche - lat={}, lon={}, poids={}", 
            latProducteur, lonProducteur, poidsKg);
        
        List<Transporteur> transporteurs = transporteurRepo.findAll();
        
        if (transporteurs.isEmpty()) {
            log.warn("⚠️ Aucun transporteur trouvé en base");
            return null;
        }
        
        List<TransporteurInfo> transporteursAvecDistance = transporteurs.stream()
            .map(t -> {
                Utilisateur user = utilisateurRepo.findById(t.getIdUtilisateur()).orElse(null);
                
                if (user == null || user.getLatitude() == null || user.getLongitude() == null) {
                    log.debug("Transporteur #{} sans coordonnées GPS", t.getId());
                    return null;
                }
                
                double distance = calculerDistance(
                    latProducteur.doubleValue(), 
                    lonProducteur.doubleValue(),
                    user.getLatitude().doubleValue(),
                    user.getLongitude().doubleValue()
                );
                
                return TransporteurInfo.builder()
                    .idTransporteur(t.getId())
                    .idUtilisateur(user.getId())
                    .nom(user.getPrenom() + " " + user.getNom())
                    .latitude(user.getLatitude())
                    .longitude(user.getLongitude())
                    .distanceKm(BigDecimal.valueOf(distance).setScale(1, RoundingMode.HALF_UP))
                    .build();
            })
            .filter(info -> info != null)
            .sorted((a, b) -> a.getDistanceKm().compareTo(b.getDistanceKm()))
            .collect(Collectors.toList());
        
        if (transporteursAvecDistance.isEmpty()) {
            log.warn("⚠️ Aucun transporteur avec coordonnées GPS trouvé");
            return null;
        }
        
        TransporteurInfo plusProche = transporteursAvecDistance.get(0);
        
        log.info("✅ Transporteur sélectionné: #{} - {} ({} km)", 
            plusProche.getIdTransporteur(), 
            plusProche.getNom(), 
            plusProche.getDistanceKm());
        
        return plusProche;
    }
    
    /**
     * Trouve le transporteur et envoie une notification
     */
    public Integer trouverEtNotifierTransporteur(BigDecimal latProducteur,
                                                   BigDecimal lonProducteur,
                                                   BigDecimal poidsKg,
                                                   Integer idCommande,
                                                   String nomProduit,
                                                   Integer quantite,
                                                   String uniteMesure,
                                                   BigDecimal prixTotal) {
        
        TransporteurInfo transporteur = trouverTransporteurLePlusProche(
            latProducteur, lonProducteur, poidsKg);
        
        if (transporteur == null) {
            log.warn("⚠️ Aucun transporteur disponible pour la commande #{}", idCommande);
            return null;
        }
        
        notificationClientService.notifierNouvelleMission(
            transporteur.getIdUtilisateur(),
            idCommande,
            nomProduit,
            quantite,
            uniteMesure,
            prixTotal,
            transporteur.getDistanceKm().doubleValue()
        );
        
        return transporteur.getIdTransporteur();
    }
    
    /**
     * Calcule la distance entre deux points GPS avec la formule de Haversine
     * @return distance en kilomètres
     */
    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TransporteurInfo {
        private Integer idTransporteur;
        private Integer idUtilisateur;
        private String nom;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private BigDecimal distanceKm;
    }
}
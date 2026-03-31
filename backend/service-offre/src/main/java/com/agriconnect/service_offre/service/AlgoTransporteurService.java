package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.model.Transporteur;
import com.agriconnect.service_offre.model.Utilisateur;
import com.agriconnect.service_offre.model.VehiculeTransporteur;
import com.agriconnect.service_offre.repository.TransporteurRepository;
import com.agriconnect.service_offre.repository.UtilisateurRepository;
import com.agriconnect.service_offre.repository.VehiculeTransporteurRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgoTransporteurService {

    private final TransporteurRepository          transporteurRepo;
    private final UtilisateurRepository           utilisateurRepo;
    private final VehiculeTransporteurRepository  vehiculeRepo;
    private final NotificationClientService       notificationClientService;
    private final SimpMessagingTemplate           messagingTemplate;

    /**
     * ALGORITHME PRINCIPAL
     *
     * Score = (distance × 0.6) + (marge_charge × 0.4)
     *
     * On cherche le transporteur dont le SCORE est le plus bas :
     *   - proche géographiquement du producteur
     *   - avec un véhicule dont la capacité correspond au poids
     *     (pas sur-dimensionné inutilement, pas insuffisant)
     *
     * @return TransporteurInfo ou null si aucun transporteur éligible
     */
    public TransporteurInfo trouverMeilleurTransporteur(BigDecimal latProducteur,
                                                         BigDecimal lonProducteur,
                                                         BigDecimal poidsKg) {

        log.info("🔍 Algo transporteur — lat={}, lon={}, poids={}kg",
            latProducteur, lonProducteur, poidsKg);

        // 1. Récupère tous les véhicules capables de porter le poids
        List<VehiculeTransporteur> vehiculesCapables =
            vehiculeRepo.findVehiculesCapables(poidsKg);

        if (vehiculesCapables.isEmpty()) {
            log.warn("⚠️ Aucun véhicule disponible avec capacité ≥ {}kg", poidsKg);
            return null;
        }

        // 2. Pour chaque véhicule, construit une entrée scorée
        List<TransporteurInfo> candidats = vehiculesCapables.stream()
            .map(vehicule -> {
                Transporteur trans = transporteurRepo.findById(vehicule.getIdTransporteur())
                    .orElse(null);
                if (trans == null) return null;

                Utilisateur user = utilisateurRepo.findById(trans.getIdUtilisateur())
                    .orElse(null);
                if (user == null || user.getLatitude() == null || user.getLongitude() == null) {
                    log.debug("Transporteur #{} sans GPS, ignoré", trans.getId());
                    return null;
                }

                double distanceKm = haversine(
                    latProducteur.doubleValue(), lonProducteur.doubleValue(),
                    user.getLatitude().doubleValue(), user.getLongitude().doubleValue()
                );

                // Marge de charge : à quel point le véhicule est surdimensionné
                // Plus la marge est faible, meilleur c'est (véhicule adapté)
                double chargeMax  = vehicule.getChargeMaxKg().doubleValue();
                double poids      = poidsKg.doubleValue();
                double margeRatio = (chargeMax - poids) / chargeMax; // 0=parfait, 1=très surdimensionné

                // Score composite : on minimise
                double score = (distanceKm * 0.6) + (margeRatio * 100 * 0.4);

                return TransporteurInfo.builder()
                    .idTransporteur(trans.getId())
                    .idUtilisateur(user.getId())
                    .idVehicule(vehicule.getId())
                    .nom(user.getPrenom() + " " + user.getNom())
                    .photoTransporteur(user.getPhoto())
                    .typeVehicule(vehicule.getTypeVehicule().name())
                    .marqueVehicule(vehicule.getMarque() + " " + vehicule.getModele())
                    .chargeMaxKg(vehicule.getChargeMaxKg())
                    .latitude(user.getLatitude())
                    .longitude(user.getLongitude())
                    .distanceKm(BigDecimal.valueOf(distanceKm).setScale(1, RoundingMode.HALF_UP))
                    .score(score)
                    .build();
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(TransporteurInfo::getScore))
            .collect(Collectors.toList());

        if (candidats.isEmpty()) {
            log.warn("⚠️ Aucun candidat valide après filtrage");
            return null;
        }

        TransporteurInfo meilleur = candidats.get(0);
        log.info("✅ Transporteur sélectionné: #{} {} — {}km — véhicule {} ({}) — score={}",
            meilleur.getIdTransporteur(), meilleur.getNom(),
            meilleur.getDistanceKm(), meilleur.getMarqueVehicule(),
            meilleur.getTypeVehicule(), String.format("%.2f", meilleur.getScore()));

        return meilleur;
    }

    /**
     * Trouve le meilleur transporteur, met son véhicule EN_MISSION,
     * envoie une notification + un événement WebSocket.
     *
     * @return idTransporteur assigné, ou null
     */
    public Integer assignerEtNotifier(BigDecimal latProducteur,
                                       BigDecimal lonProducteur,
                                       BigDecimal poidsKg,
                                       Integer idCommande,
                                       String nomProduit,
                                       Integer quantite,
                                       String uniteMesure,
                                       BigDecimal prixTotal) {

        TransporteurInfo candidat = trouverMeilleurTransporteur(
            latProducteur, lonProducteur, poidsKg);

        if (candidat == null) return null;

        // Marquer le véhicule en mission
        vehiculeRepo.findById(candidat.getIdVehicule()).ifPresent(v -> {
            v.setEnMission(true);
            vehiculeRepo.save(v);
        });

        // Notification push
        notificationClientService.notifierNouvelleMission(
            candidat.getIdUtilisateur(),
            idCommande,
            nomProduit,
            quantite,
            uniteMesure,
            prixTotal,
            candidat.getDistanceKm().doubleValue()
        );

        // WebSocket — le transporteur reçoit sa mission en temps réel
        Map<String, Object> payload = new HashMap<>();
        payload.put("type",          "NOUVELLE_MISSION");
        payload.put("idCommande",    idCommande);
        payload.put("nomProduit",    nomProduit);
        payload.put("quantite",      quantite);
        payload.put("unite",         uniteMesure);
        payload.put("prixTotal",     prixTotal);
        payload.put("distanceKm",    candidat.getDistanceKm());
        payload.put("vehicule",      candidat.getMarqueVehicule());

        messagingTemplate.convertAndSend(
            "/topic/transporteur/" + candidat.getIdUtilisateur() + "/mission",
            payload
        );

        log.info("📡 Mission WebSocket envoyée au transporteur #{}", candidat.getIdUtilisateur());

        return candidat.getIdTransporteur();
    }

    /**
     * Libère le véhicule quand une mission se termine ou est refusée.
     */
    public void libererVehicule(Integer idTransporteur) {
        vehiculeRepo.findByIdTransporteurAndDisponibleTrueAndEnMissionFalse(idTransporteur);
        // On cherche le premier véhicule en mission de ce transporteur et on le libère
        vehiculeRepo.findByIdTransporteurOrderByChargeMaxKgDesc(idTransporteur)
            .stream()
            .filter(v -> Boolean.TRUE.equals(v.getEnMission()))
            .findFirst()
            .ifPresent(v -> {
                v.setEnMission(false);
                vehiculeRepo.save(v);
                log.info("🔓 Véhicule #{} ({} {}) libéré", v.getId(), v.getMarque(), v.getModele());
            });
    }

    // ── Formule de Haversine ─────────────────────────────────────
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ── DTO interne ──────────────────────────────────────────────
    @Data
    @Builder
    public static class TransporteurInfo {
        private Integer    idTransporteur;
        private Integer    idUtilisateur;
        private Integer    idVehicule;
        private String     nom;
        private String     photoTransporteur;
        private String     typeVehicule;
        private String     marqueVehicule;
        private BigDecimal chargeMaxKg;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private BigDecimal distanceKm;
        private double     score;
    }
}
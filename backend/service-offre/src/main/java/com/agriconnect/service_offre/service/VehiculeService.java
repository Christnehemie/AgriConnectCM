package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.dto.VehiculeDTO;
import com.agriconnect.service_offre.model.Transporteur;
import com.agriconnect.service_offre.model.VehiculeTransporteur;
import com.agriconnect.service_offre.model.VehiculeTransporteur.TypeVehicule;
import com.agriconnect.service_offre.repository.TransporteurRepository;
import com.agriconnect.service_offre.repository.VehiculeTransporteurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehiculeService {

    private final VehiculeTransporteurRepository vehiculeRepo;
    private final TransporteurRepository transporteurRepo;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Lister mes véhicules ─────────────────────────────────────
    public List<VehiculeDTO> getMesVehicules(Integer idUtilisateur) {
        Transporteur t = transporteurRepo.findByIdUtilisateur(idUtilisateur)
            .orElseThrow(() -> new RuntimeException("Profil transporteur introuvable"));
        return vehiculeRepo.findByIdTransporteurOrderByChargeMaxKgDesc(t.getId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Lister mes véhicules disponibles ─────────────────────────
    public List<VehiculeDTO> getMesVehiculesDisponibles(Integer idUtilisateur) {
        Transporteur t = transporteurRepo.findByIdUtilisateur(idUtilisateur)
            .orElseThrow(() -> new RuntimeException("Profil transporteur introuvable"));
        return vehiculeRepo
            .findByIdTransporteurAndDisponibleTrueAndEnMissionFalse(t.getId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Ajouter un véhicule ──────────────────────────────────────
    @Transactional
    public VehiculeDTO ajouterVehicule(Integer idUtilisateur,
                                        String marque, String modele,
                                        String immatriculation, String typeVehicule,
                                        BigDecimal chargeMaxKg, BigDecimal volumeMaxM3,
                                        String photoVehicule) {

        Transporteur t = transporteurRepo.findByIdUtilisateur(idUtilisateur)
            .orElseThrow(() -> new RuntimeException("Profil transporteur introuvable"));

        if (vehiculeRepo.existsByImmatriculation(immatriculation))
            throw new RuntimeException("Immatriculation déjà enregistrée : " + immatriculation);

        TypeVehicule type;
        try {
            type = TypeVehicule.valueOf(typeVehicule.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de véhicule invalide. Valeurs acceptées : MOTO, TRICYCLE, CAMIONNETTE, CAMION, PICK_UP");
        }

        VehiculeTransporteur vehicule = VehiculeTransporteur.builder()
            .idTransporteur(t.getId())
            .marque(marque)
            .modele(modele)
            .immatriculation(immatriculation.toUpperCase())
            .typeVehicule(type)
            .chargeMaxKg(chargeMaxKg)
            .volumeMaxM3(volumeMaxM3)
            .disponible(true)
            .enMission(false)
            .photoVehicule(photoVehicule)
            .build();

        VehiculeDTO dto = toDTO(vehiculeRepo.save(vehicule));

        // WebSocket — notifie en temps réel
        messagingTemplate.convertAndSend(
            "/topic/transporteur/" + t.getId() + "/vehicules",
            dto
        );

        log.info("✅ Véhicule ajouté: {} {} ({}) par transporteur #{}",
            marque, modele, immatriculation, t.getId());

        return dto;
    }

    // ── Modifier un véhicule ─────────────────────────────────────
    @Transactional
    public VehiculeDTO modifierVehicule(Integer idVehicule, Integer idUtilisateur,
                                         String marque, String modele,
                                         String immatriculation, String typeVehicule,
                                         BigDecimal chargeMaxKg, BigDecimal volumeMaxM3,
                                         Boolean disponible, String photoVehicule) {

        Transporteur t = transporteurRepo.findByIdUtilisateur(idUtilisateur)
            .orElseThrow(() -> new RuntimeException("Profil transporteur introuvable"));

        VehiculeTransporteur v = vehiculeRepo.findById(idVehicule)
            .orElseThrow(() -> new RuntimeException("Véhicule introuvable"));

        if (!v.getIdTransporteur().equals(t.getId()))
            throw new RuntimeException("Ce véhicule ne vous appartient pas");

        if (marque != null)         v.setMarque(marque);
        if (modele != null)         v.setModele(modele);
        if (immatriculation != null) {
            String immat = immatriculation.toUpperCase();
            if (!immat.equals(v.getImmatriculation()) && vehiculeRepo.existsByImmatriculation(immat))
                throw new RuntimeException("Immatriculation déjà enregistrée : " + immat);
            v.setImmatriculation(immat);
        }
        if (typeVehicule != null) {
            try {
                v.setTypeVehicule(TypeVehicule.valueOf(typeVehicule.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Type invalide");
            }
        }
        if (chargeMaxKg != null)    v.setChargeMaxKg(chargeMaxKg);
        if (volumeMaxM3 != null)    v.setVolumeMaxM3(volumeMaxM3);
        if (disponible != null)     v.setDisponible(disponible);
        if (photoVehicule != null && !photoVehicule.isBlank()) v.setPhotoVehicule(photoVehicule);

        VehiculeDTO dto = toDTO(vehiculeRepo.save(v));

        // WebSocket — mise à jour temps réel
        messagingTemplate.convertAndSend(
            "/topic/transporteur/" + t.getId() + "/vehicules",
            dto
        );

        log.info("✏️ Véhicule #{} modifié par transporteur #{}", idVehicule, t.getId());
        return dto;
    }

    // ── Supprimer un véhicule ────────────────────────────────────
    @Transactional
    public void supprimerVehicule(Integer idVehicule, Integer idUtilisateur) {
        Transporteur t = transporteurRepo.findByIdUtilisateur(idUtilisateur)
            .orElseThrow(() -> new RuntimeException("Profil transporteur introuvable"));

        VehiculeTransporteur v = vehiculeRepo.findById(idVehicule)
            .orElseThrow(() -> new RuntimeException("Véhicule introuvable"));

        if (!v.getIdTransporteur().equals(t.getId()))
            throw new RuntimeException("Ce véhicule ne vous appartient pas");

        if (Boolean.TRUE.equals(v.getEnMission()))
            throw new RuntimeException("Impossible de supprimer un véhicule en mission");

        vehiculeRepo.delete(v);

        // WebSocket — notifie la suppression
        messagingTemplate.convertAndSend(
            "/topic/transporteur/" + t.getId() + "/vehicules/supprime",
            idVehicule
        );

        log.info("🗑️ Véhicule #{} supprimé par transporteur #{}", idVehicule, t.getId());
    }

    // ── Mapper ───────────────────────────────────────────────────
    public VehiculeDTO toDTO(VehiculeTransporteur v) {
        return VehiculeDTO.builder()
            .id(v.getId())
            .idTransporteur(v.getIdTransporteur())
            .marque(v.getMarque())
            .modele(v.getModele())
            .immatriculation(v.getImmatriculation())
            .typeVehicule(v.getTypeVehicule())
            .chargeMaxKg(v.getChargeMaxKg())
            .volumeMaxM3(v.getVolumeMaxM3())
            .disponible(v.getDisponible())
            .enMission(v.getEnMission())
            .dateAjout(v.getDateAjout())
            .photoVehicule(v.getPhotoVehicule())
            .build();
    }
}
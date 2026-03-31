package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.dto.*;
import com.agriconnect.service_offre.model.*;
import com.agriconnect.service_offre.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeOffreRepository commandeRepo;
    private final PublicationRepository   pubRepo;
    private final ProduitRepository       produitRepo;
    private final AcheteurRepository      acheteurRepo;
    private final ProducteurRepository    producteurRepo;
    private final TransporteurRepository  transporteurRepo;
    private final UtilisateurRepository   userRepo;
    private final AlgoTransporteurService algoTransporteur;
    private final SimpMessagingTemplate   messagingTemplate;

    // ═══════════════════════════════════════════════════════
    // CRÉER UNE COMMANDE
    // ═══════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO commander(Integer idPublication, Integer idUserAcheteur,
                                  Integer quantite, String methodePaiement) {

        log.info("📝 Commande — pub={}, acheteur={}, qte={}", idPublication, idUserAcheteur, quantite);

        // 1. Validation publication
        Publication pub = pubRepo.findById(idPublication)
            .orElseThrow(() -> new RuntimeException("Publication introuvable"));
        if (!"OFFRE".equals(pub.getTypePub()))
            throw new RuntimeException("Cette publication n'est pas une offre");
        if (!Boolean.TRUE.equals(pub.getDisponible()))
            throw new RuntimeException("Cette offre n'est plus disponible");

        // 2. Validation produit + stock
        Produit produit = produitRepo.findById(pub.getIdProduit())
            .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        if (produit.getStockDisponible() != null && quantite > produit.getStockDisponible())
            throw new RuntimeException("Stock insuffisant (" + produit.getStockDisponible() + " disponibles)");

        // 3. Acteurs
        Acheteur acheteur = acheteurRepo.findByIdUtilisateur(idUserAcheteur)
            .orElseThrow(() -> new RuntimeException("Profil acheteur introuvable"));
        Producteur producteur = producteurRepo.findByIdUtilisateur(pub.getIdPosteur())
            .orElseThrow(() -> new RuntimeException("Profil producteur introuvable"));

        // 4. Calculs
        BigDecimal poidsTotal = produit.getPoidsUnitaire() != null
            ? produit.getPoidsUnitaire().multiply(BigDecimal.valueOf(quantite))
            : BigDecimal.ONE;
        BigDecimal prixEstime = pub.getPrixOffre() != null
            ? pub.getPrixOffre().multiply(BigDecimal.valueOf(quantite))
            : BigDecimal.ZERO;

        // 5. Coordonnées producteur
        Utilisateur userProd = userRepo.findById(pub.getIdPosteur()).orElse(null);
        BigDecimal latProd = userProd != null ? userProd.getLatitude()  : null;
        BigDecimal lonProd = userProd != null ? userProd.getLongitude() : null;

        // 6. Assignation transporteur (UN SEUL APPEL — bug corrigé)
        Integer idTransporteur = null;
        if (latProd != null && lonProd != null) {
            idTransporteur = algoTransporteur.assignerEtNotifier(
                latProd, lonProd, poidsTotal,
                null, // idCommande pas encore connu
                produit.getNom(), quantite, pub.getUniteMesure(), prixEstime
            );
        } else {
            log.warn("⚠️ Producteur #{} sans GPS — transporteur non assigné", pub.getIdPosteur());
        }

        String statut = idTransporteur != null ? "TRANSPORTEUR_ASSIGNE" : "EN_ATTENTE";

        // 7. Persistance commande
        CommandeOffre commande = CommandeOffre.builder()
            .idPublication(idPublication)
            .idAcheteur(acheteur.getId())
            .idProducteur(producteur.getId())
            .quantite(quantite)
            .poidsTotal(poidsTotal)
            .prixEstime(prixEstime)
            .idTransporteur(idTransporteur)
            .statut(statut)
            .notes(methodePaiement)
            .build();

        CommandeOffre saved = commandeRepo.save(commande);

        // 8. Si transporteur assigné, mettre à jour la notif avec le vrai idCommande
        if (idTransporteur != null) {
            notifierMissionAvecId(saved, produit, pub, idTransporteur);
        }

        // 9. WebSocket — notifie l'acheteur et le producteur en temps réel
        CommandeDTO dto = toDTO(saved);
        broadcastCommandeUpdate(dto, "NOUVELLE_COMMANDE");

        log.info("✅ Commande #{} créée — statut={}", saved.getId(), saved.getStatut());
        return dto;
    }

    // ═══════════════════════════════════════════════════════
    // ACCEPTER UNE MISSION (transporteur)
    // ═══════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO accepterCommande(Integer idCommande, Integer idUserTransporteur) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        Transporteur trans = transporteurRepo.findByIdUtilisateur(idUserTransporteur)
            .orElseThrow(() -> new RuntimeException("Profil transporteur introuvable"));

        if (!trans.getId().equals(cmd.getIdTransporteur()))
            throw new RuntimeException("Cette commande ne vous est pas assignée");

        cmd.setStatut("EN_COURS");
        CommandeDTO dto = toDTO(commandeRepo.save(cmd));
        broadcastCommandeUpdate(dto, "COMMANDE_ACCEPTEE");

        log.info("✅ Transporteur #{} accepte la commande #{}", trans.getId(), idCommande);
        return dto;
    }

    // ═══════════════════════════════════════════════════════
    // REFUSER UNE MISSION → réassignation automatique
    // ═══════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO refuserCommande(Integer idCommande, Integer idUserTransporteur) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        Integer ancienTransporteurId = cmd.getIdTransporteur();

        // Libérer le véhicule de l'ancien transporteur
        if (ancienTransporteurId != null) {
            algoTransporteur.libererVehicule(ancienTransporteurId);
        }

        // Récupérer infos pour réassigner
        Publication pub    = pubRepo.findById(cmd.getIdPublication()).orElse(null);
        Produit     produit = pub != null && pub.getIdProduit() != null
            ? produitRepo.findById(pub.getIdProduit()).orElse(null) : null;
        Utilisateur userProd = pub != null
            ? userRepo.findById(pub.getIdPosteur()).orElse(null) : null;

        BigDecimal poidsTotal = cmd.getPoidsTotal() != null ? cmd.getPoidsTotal() : BigDecimal.ONE;
        BigDecimal prixEstime = cmd.getPrixEstime() != null ? cmd.getPrixEstime() : BigDecimal.ZERO;
        BigDecimal latProd    = userProd != null ? userProd.getLatitude()  : null;
        BigDecimal lonProd    = userProd != null ? userProd.getLongitude() : null;

        Integer idNouveauTrans = null;
        if (latProd != null && lonProd != null) {
            idNouveauTrans = algoTransporteur.assignerEtNotifier(
                latProd, lonProd, poidsTotal,
                cmd.getId(),
                produit != null ? produit.getNom() : "produit",
                cmd.getQuantite(),
                pub != null ? pub.getUniteMesure() : "kg",
                prixEstime
            );
        }

        // Évite de réassigner le même transporteur qui vient de refuser
        if (idNouveauTrans != null && idNouveauTrans.equals(ancienTransporteurId)) {
            idNouveauTrans = null;
        }

        if (idNouveauTrans != null) {
            cmd.setIdTransporteur(idNouveauTrans);
            cmd.setStatut("TRANSPORTEUR_ASSIGNE");
        } else {
            cmd.setIdTransporteur(null);
            cmd.setStatut("EN_ATTENTE");
        }

        CommandeDTO dto = toDTO(commandeRepo.save(cmd));
        broadcastCommandeUpdate(dto, "COMMANDE_REASSIGNEE");

        log.info("🔄 Commande #{} refusée — nouveau transporteur: {}", idCommande, cmd.getIdTransporteur());
        return dto;
    }

    // ═══════════════════════════════════════════════════════
    // MARQUER LIVRÉ
    // ═══════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO marquerLivre(Integer idCommande, Integer idUserTransporteur) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        cmd.setStatut("LIVRE");

        // Libérer le véhicule
        if (cmd.getIdTransporteur() != null) {
            algoTransporteur.libererVehicule(cmd.getIdTransporteur());
        }

        // Décrémenter le stock produit
        pubRepo.findById(cmd.getIdPublication()).ifPresent(pub -> {
            if (pub.getIdProduit() != null) {
                produitRepo.findById(pub.getIdProduit()).ifPresent(produit -> {
                    if (produit.getStockDisponible() != null) {
                        int nouveau = Math.max(0, produit.getStockDisponible() - cmd.getQuantite());
                        produit.setStockDisponible(nouveau);
                        produitRepo.save(produit);
                        if (nouveau <= 0) {
                            pub.setDisponible(false);
                            pubRepo.save(pub);
                        }
                    }
                });
            }
        });

        CommandeDTO dto = toDTO(commandeRepo.save(cmd));
        broadcastCommandeUpdate(dto, "COMMANDE_LIVREE");

        log.info("✅ Commande #{} livrée", idCommande);
        return dto;
    }

    // ═══════════════════════════════════════════════════════
    // ANNULER
    // ═══════════════════════════════════════════════════════
    @Transactional
    public CommandeDTO annuler(Integer idCommande, Integer idUser) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if (cmd.getIdTransporteur() != null) {
            algoTransporteur.libererVehicule(cmd.getIdTransporteur());
        }

        cmd.setStatut("ANNULE");
        CommandeDTO dto = toDTO(commandeRepo.save(cmd));
        broadcastCommandeUpdate(dto, "COMMANDE_ANNULEE");

        log.info("⚠️ Commande #{} annulée", idCommande);
        return dto;
    }

    // ═══════════════════════════════════════════════════════
    // LISTES
    // ═══════════════════════════════════════════════════════
    public List<CommandeDTO> getMesCommandes(Integer idUserAcheteur) {
        Acheteur a = acheteurRepo.findByIdUtilisateur(idUserAcheteur).orElse(null);
        if (a == null) return List.of();
        return commandeRepo.findByIdAcheteurOrderByDateCommandeDesc(a.getId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CommandeDTO> getMesVentes(Integer idUserProducteur) {
        Producteur p = producteurRepo.findByIdUtilisateur(idUserProducteur).orElse(null);
        if (p == null) return List.of();
        return commandeRepo.findByIdProducteurOrderByDateCommandeDesc(p.getId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<CommandeDTO> getMesMissions(Integer idUserTransporteur) {
        Transporteur t = transporteurRepo.findByIdUtilisateur(idUserTransporteur).orElse(null);
        if (t == null) return List.of();
        return commandeRepo.findByIdTransporteurOrderByDateCommandeDesc(t.getId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    // PRIVÉ — helpers
    // ═══════════════════════════════════════════════════════

    /** Envoie l'idCommande réel à la notif après save */
    private void notifierMissionAvecId(CommandeOffre saved, Produit produit,
                                        Publication pub, Integer idTransporteur) {
        transporteurRepo.findById(idTransporteur).ifPresent(t ->
            userRepo.findById(t.getIdUtilisateur()).ifPresent(u -> {
                Map<String, Object> payload = new HashMap<>();
                payload.put("type",       "MISSION_ID_CONFIRME");
                payload.put("idCommande", saved.getId());
                messagingTemplate.convertAndSend(
                    "/topic/transporteur/" + u.getId() + "/mission",
                    payload
                );
            })
        );
    }

    /** Broadcast WebSocket vers acheteur, producteur et transporteur */
    private void broadcastCommandeUpdate(CommandeDTO dto, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event",   eventType);
        payload.put("commande", dto);

        // Canal global (dashboard admin)
        messagingTemplate.convertAndSend("/topic/commandes", payload);

        // Canaux personnalisés par utilisateur
        if (dto.getIdAcheteur() != null) {
            Acheteur a = acheteurRepo.findById(dto.getIdAcheteur()).orElse(null);
            if (a != null) messagingTemplate.convertAndSend(
                "/topic/user/" + a.getIdUtilisateur() + "/commandes", payload);
        }
        if (dto.getIdProducteur() != null) {
            Producteur p = producteurRepo.findById(dto.getIdProducteur()).orElse(null);
            if (p != null) messagingTemplate.convertAndSend(
                "/topic/user/" + p.getIdUtilisateur() + "/commandes", payload);
        }
        if (dto.getIdTransporteur() != null) {
            transporteurRepo.findById(dto.getIdTransporteur()).ifPresent(t ->
                messagingTemplate.convertAndSend(
                    "/topic/user/" + t.getIdUtilisateur() + "/commandes", payload));
        }
    }

    // ── Mapper ───────────────────────────────────────────────────
    private CommandeDTO toDTO(CommandeOffre c) {
        CommandeDTO dto = CommandeDTO.builder()
            .id(c.getId())
            .idPublication(c.getIdPublication())
            .idAcheteur(c.getIdAcheteur())
            .idProducteur(c.getIdProducteur())
            .idTransporteur(c.getIdTransporteur())
            .quantite(c.getQuantite())
            .poidsTotal(c.getPoidsTotal())
            .prixEstime(c.getPrixEstime())
            .statut(c.getStatut())
            .dateCommande(c.getDateCommande())
            .methodePaiement(c.getNotes())
            .build();

        pubRepo.findById(c.getIdPublication()).ifPresent(p -> {
            dto.setTitreOffre(p.getContenu());
            dto.setImageOffre(p.getImage());
        });

        if (c.getIdAcheteur() != null)
            acheteurRepo.findById(c.getIdAcheteur()).ifPresent(a ->
                userRepo.findById(a.getIdUtilisateur()).ifPresent(u -> {
                    dto.setNomAcheteur(u.getPrenom() + " " + u.getNom());
                    dto.setPhotoAcheteur(u.getPhoto());
                    dto.setAdresseLivraison(a.getLocalLivraison());
                }));

        if (c.getIdProducteur() != null)
            producteurRepo.findById(c.getIdProducteur()).ifPresent(p ->
                userRepo.findById(p.getIdUtilisateur()).ifPresent(u -> {
                    dto.setNomProducteur(u.getPrenom() + " " + u.getNom());
                    dto.setPhotoProducteur(u.getPhoto());
                }));

        if (c.getIdTransporteur() != null)
            transporteurRepo.findById(c.getIdTransporteur()).ifPresent(t ->
                userRepo.findById(t.getIdUtilisateur()).ifPresent(u -> {
                    dto.setNomTransporteur(u.getPrenom() + " " + u.getNom());
                    dto.setPhotoTransporteur(u.getPhoto());
                }));

        return dto;
    }
}
package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.dto.*;
import com.agriconnect.service_offre.model.*;
import com.agriconnect.service_offre.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeOffreRepository commandeRepo;
    private final PublicationRepository pubRepo;
    private final ProduitRepository produitRepo;
    private final AcheteurRepository acheteurRepo;
    private final ProducteurRepository producteurRepo;
    private final TransporteurRepository transporteurRepo;
    private final UtilisateurRepository userRepo;
    private final AlgoTransporteurService algoTransporteur;

    @Transactional
    public CommandeDTO commander(Integer idPublication, Integer idUserAcheteur,
                                  Integer quantite, String methodePaiement) {

        log.info("📝 Création commande - Publication: {}, Acheteur: {}, Quantité: {}", 
            idPublication, idUserAcheteur, quantite);

        Publication pub = pubRepo.findById(idPublication)
            .orElseThrow(() -> new RuntimeException("Publication introuvable"));
        
        if (!"OFFRE".equals(pub.getTypePub()))
            throw new RuntimeException("Cette publication n'est pas une offre");
        if (!Boolean.TRUE.equals(pub.getDisponible()))
            throw new RuntimeException("Cette offre n'est plus disponible");

        Produit produit = produitRepo.findById(pub.getIdProduit())
            .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        
        if (produit.getStockDisponible() != null && quantite > produit.getStockDisponible())
            throw new RuntimeException("Stock insuffisant");

        Acheteur acheteur = acheteurRepo.findByIdUtilisateur(idUserAcheteur)
            .orElseThrow(() -> new RuntimeException("Profil acheteur introuvable"));
        
        Producteur producteur = producteurRepo.findByIdUtilisateur(pub.getIdPosteur())
            .orElseThrow(() -> new RuntimeException("Profil producteur introuvable"));

        BigDecimal poidsTotal = produit.getPoidsUnitaire() != null
            ? produit.getPoidsUnitaire().multiply(BigDecimal.valueOf(quantite))
            : BigDecimal.ONE;
        
        BigDecimal prixEstime = pub.getPrixOffre() != null
            ? pub.getPrixOffre().multiply(BigDecimal.valueOf(quantite))
            : BigDecimal.ZERO;

        Utilisateur userProd = userRepo.findById(pub.getIdPosteur()).orElse(null);
        
        BigDecimal latProducteur = userProd != null ? userProd.getLatitude() : null;
        BigDecimal lonProducteur = userProd != null ? userProd.getLongitude() : null;
        
        if (latProducteur == null || lonProducteur == null) {
            log.warn("⚠️ Producteur #{} sans coordonnées GPS", pub.getIdPosteur());
        }
        
        Integer idTransporteur = null;
        
        if (latProducteur != null && lonProducteur != null) {
            idTransporteur = algoTransporteur.trouverEtNotifierTransporteur(
                latProducteur,
                lonProducteur,
                poidsTotal,
                null,
                produit.getNom(),
                quantite,
                pub.getUniteMesure(),
                prixEstime
            );
        }
        
        String statut = idTransporteur != null ? "TRANSPORTEUR_ASSIGNE" : "EN_ATTENTE";
        
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
        
        if (idTransporteur != null && latProducteur != null && lonProducteur != null) {
            algoTransporteur.trouverEtNotifierTransporteur(
                latProducteur,
                lonProducteur,
                poidsTotal,
                saved.getId(),
                produit.getNom(),
                quantite,
                pub.getUniteMesure(),
                prixEstime
            );
        }
        
        log.info("✅ Commande #{} créée - statut: {}", saved.getId(), saved.getStatut());
        return toDTO(saved);
    }

    @Transactional
    public CommandeDTO accepterCommande(Integer idCommande, Integer idUserTransporteur) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        
        Transporteur trans = transporteurRepo.findByIdUtilisateur(idUserTransporteur)
            .orElseThrow(() -> new RuntimeException("Profil transporteur introuvable"));

        if (!trans.getId().equals(cmd.getIdTransporteur()))
            throw new RuntimeException("Cette commande ne vous est pas assignée");

        cmd.setStatut("EN_COURS");
        CommandeOffre saved = commandeRepo.save(cmd);
        
        log.info("✅ Transporteur #{} a accepté la commande #{}", trans.getId(), idCommande);
        return toDTO(saved);
    }

    @Transactional
    public CommandeDTO refuserCommande(Integer idCommande, Integer idUserTransporteur) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        Publication pub = pubRepo.findById(cmd.getIdPublication()).orElse(null);
        Produit produit = pub != null && pub.getIdProduit() != null
            ? produitRepo.findById(pub.getIdProduit()).orElse(null) : null;
        Utilisateur userProd = pub != null
            ? userRepo.findById(pub.getIdPosteur()).orElse(null) : null;

        BigDecimal poidsTotal = cmd.getPoidsTotal();
        if (poidsTotal == null && produit != null && cmd.getQuantite() != null) {
            poidsTotal = produit.getPoidsUnitaire() != null
                ? produit.getPoidsUnitaire().multiply(BigDecimal.valueOf(cmd.getQuantite()))
                : BigDecimal.ONE;
        }
        
        BigDecimal prixEstime = cmd.getPrixEstime();
        if (prixEstime == null && pub != null && pub.getPrixOffre() != null && cmd.getQuantite() != null) {
            prixEstime = pub.getPrixOffre().multiply(BigDecimal.valueOf(cmd.getQuantite()));
        }
        
        BigDecimal latProd = userProd != null ? userProd.getLatitude() : null;
        BigDecimal lonProd = userProd != null ? userProd.getLongitude() : null;
        
        Integer idNouveauTrans = null;
        if (latProd != null && lonProd != null) {
            idNouveauTrans = algoTransporteur.trouverEtNotifierTransporteur(
                latProd,
                lonProd,
                poidsTotal,
                cmd.getId(),
                produit != null ? produit.getNom() : "produit",
                cmd.getQuantite(),
                pub != null ? pub.getUniteMesure() : "kg",
                prixEstime
            );
        }

        if (idNouveauTrans != null && !idNouveauTrans.equals(cmd.getIdTransporteur())) {
            cmd.setIdTransporteur(idNouveauTrans);
            cmd.setStatut("TRANSPORTEUR_ASSIGNE");
        } else {
            cmd.setIdTransporteur(null);
            cmd.setStatut("EN_ATTENTE");
        }

        log.info("Commande #{} refusée - nouveau transporteur: {}", idCommande, cmd.getIdTransporteur());
        return toDTO(commandeRepo.save(cmd));
    }

    @Transactional
    public CommandeDTO marquerLivre(Integer idCommande, Integer idUserTransporteur) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        
        cmd.setStatut("LIVRE");
        
        Publication pub = pubRepo.findById(cmd.getIdPublication()).orElse(null);
        if (pub != null && pub.getIdProduit() != null) {
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
        
        log.info("✅ Commande #{} livrée", idCommande);
        return toDTO(commandeRepo.save(cmd));
    }

    @Transactional
    public CommandeDTO annuler(Integer idCommande, Integer idUser) {
        CommandeOffre cmd = commandeRepo.findById(idCommande)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        
        cmd.setStatut("ANNULE");
        log.info("⚠️ Commande #{} annulée", idCommande);
        return toDTO(commandeRepo.save(cmd));
    }

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

        if (c.getIdAcheteur() != null) {
            acheteurRepo.findById(c.getIdAcheteur()).ifPresent(a ->
                userRepo.findById(a.getIdUtilisateur()).ifPresent(u -> {
                    dto.setNomAcheteur(u.getPrenom() + " " + u.getNom());
                    dto.setPhotoAcheteur(u.getPhoto());
                    dto.setAdresseLivraison(a.getLocalLivraison());
                }));
        }

        if (c.getIdProducteur() != null) {
            producteurRepo.findById(c.getIdProducteur()).ifPresent(p ->
                userRepo.findById(p.getIdUtilisateur()).ifPresent(u -> {
                    dto.setNomProducteur(u.getPrenom() + " " + u.getNom());
                    dto.setPhotoProducteur(u.getPhoto());
                }));
        }

        if (c.getIdTransporteur() != null) {
            transporteurRepo.findById(c.getIdTransporteur()).ifPresent(t ->
                userRepo.findById(t.getIdUtilisateur()).ifPresent(u -> {
                    dto.setNomTransporteur(u.getPrenom() + " " + u.getNom());
                    dto.setPhotoTransporteur(u.getPhoto());
                }));
        }

        return dto;
    }
}
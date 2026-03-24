package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.dto.*;
import com.agriconnect.service_offre.model.*;
import com.agriconnect.service_offre.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository   pubRepo;
    private final ProduitRepository       produitRepo;
    private final CommentaireRepository   commRepo;
    private final LikeRepository          likeRepo;
    private final UtilisateurRepository   userRepo;
    private final CommandeOffreRepository commandeRepo;

    // ── Feed tous types ──────────────────────────────────────────
    public Page<PublicationDTO> getFeed(Integer idUser, int page, int size) {
        Pageable p = PageRequest.of(page, size,
            Sort.by(Sort.Direction.DESC, "datePublication"));
        return pubRepo.findAllByOrderByDatePublicationDesc(p)
            .map(pub -> toDTO(pub, idUser));
    }

    // ── Feed filtré par type ─────────────────────────────────────
    public Page<PublicationDTO> getFeedParType(String type, Integer idUser, int page, int size) {
        Pageable p = PageRequest.of(page, size,
            Sort.by(Sort.Direction.DESC, "datePublication"));
        Page<Publication> pubs = "OFFRE".equals(type)
            ? pubRepo.findByTypePubAndDisponibleTrueOrderByDatePublicationDesc(type, p)
            : pubRepo.findByTypePubOrderByDatePublicationDesc(type, p);
        return pubs.map(pub -> toDTO(pub, idUser));
    }

    // ── Publications d'un utilisateur ────────────────────────────
    public List<PublicationDTO> getByUser(Integer idPosteur, Integer idUser) {
        return pubRepo.findByIdPosteurOrderByDatePublicationDesc(idPosteur)
            .stream().map(p -> toDTO(p, idUser)).collect(Collectors.toList());
    }

    // ── Créer une publication ────────────────────────────────────
    // imageUrl = URL de l'image uploadée côté frontend
    public PublicationDTO creer(Integer idUser, String contenu, String typePub,
                                 Integer idProduit, Integer quantiteOfferte,
                                 BigDecimal prixOffre, String uniteMesure,
                                 String imageUrl) {

        Publication pub = Publication.builder()
            .idPosteur(idUser)
            .contenu(contenu)
            .image(imageUrl)
            .typePub(typePub != null ? typePub : "POST")
            .idProduit(idProduit)
            .quantiteOfferte(quantiteOfferte)
            .prixOffre(prixOffre)
            .uniteMesure(uniteMesure != null ? uniteMesure : "kg")
            .disponible(true)
            .nbLikes(0)
            .build();

        return toDTO(pubRepo.save(pub), idUser);
    }

    // ── Supprimer ────────────────────────────────────────────────
    public void supprimer(Integer idPub, Integer idUser) {
        pubRepo.findById(idPub)
            .filter(p -> p.getIdPosteur().equals(idUser))
            .ifPresent(pubRepo::delete);
    }

    // ── Marquer indisponible ─────────────────────────────────────
    public void marquerIndisponible(Integer idPub, Integer idUser) {
        pubRepo.findById(idPub).ifPresent(p -> {
            if (p.getIdPosteur().equals(idUser)) {
                p.setDisponible(false);
                pubRepo.save(p);
            }
        });
    }

    // ── Like / Unlike ────────────────────────────────────────────
    public PublicationDTO toggleLike(Integer idPub, Integer idUser) {
        Publication pub = pubRepo.findById(idPub)
            .orElseThrow(() -> new RuntimeException("Publication introuvable"));

        boolean liked = likeRepo.existsByIdPublicationAndIdUtilisateur(idPub, idUser);
        if (liked) {
            likeRepo.findByIdPublicationAndIdUtilisateur(idPub, idUser)
                .ifPresent(likeRepo::delete);
            pub.setNbLikes(Math.max(0, pub.getNbLikes() - 1));
        } else {
            likeRepo.save(LikePublication.builder()
                .idPublication(idPub).idUtilisateur(idUser).build());
            pub.setNbLikes(pub.getNbLikes() + 1);
        }
        return toDTO(pubRepo.save(pub), idUser);
    }

    // ── Commenter ────────────────────────────────────────────────
    public CommentaireDTO commenter(Integer idPub, Integer idUser, String contenu) {
        return toCommentaireDTO(commRepo.save(Commentaire.builder()
            .idPublication(idPub).idUtilisateur(idUser).contenu(contenu).build()));
    }

    public List<CommentaireDTO> getCommentaires(Integer idPub) {
        return commRepo.findByIdPublicationOrderByDateCommentaireAsc(idPub)
            .stream().map(this::toCommentaireDTO).collect(Collectors.toList());
    }

    // ── Recherche ────────────────────────────────────────────────
    public List<PublicationDTO> rechercher(String q, String type, Integer idUser) {
        return pubRepo.rechercher(q, type)
            .stream().map(p -> toDTO(p, idUser)).collect(Collectors.toList());
    }

    // ── Mapper ───────────────────────────────────────────────────
    public PublicationDTO toDTO(Publication p, Integer idUser) {
        Utilisateur u = p.getPosteur() != null ? p.getPosteur()
            : userRepo.findById(p.getIdPosteur()).orElse(null);

        boolean liked = idUser != null &&
            likeRepo.existsByIdPublicationAndIdUtilisateur(p.getId(), idUser);
        long nbComm = commRepo.countByIdPublication(p.getId());
        long nbCmd  = commandeRepo
            .findByIdProducteurOrderByDateCommandeDesc(p.getIdPosteur())
            .stream().filter(c -> c.getIdPublication().equals(p.getId())).count();

        String nomProduit = null, imageProduit = null;
        BigDecimal poidsUnitaire = null;
        if (p.getIdProduit() != null) {
            Produit prod = p.getProduit() != null ? p.getProduit()
                : produitRepo.findById(p.getIdProduit()).orElse(null);
            if (prod != null) {
                nomProduit    = prod.getNom();
                imageProduit  = prod.getImage();
                poidsUnitaire = prod.getPoidsUnitaire();
            }
        }

        return PublicationDTO.builder()
            .id(p.getId()).contenu(p.getContenu()).image(p.getImage())
            .datePublication(p.getDatePublication()).nbLikes(p.getNbLikes())
            .nbCommentaires(nbComm).likeParMoi(liked).nbCommandes(nbCmd)
            .idPosteur(p.getIdPosteur())
            .nomPosteur(u != null ? u.getPrenom() + " " + u.getNom() : "Utilisateur")
            .photoPosteur(u != null ? u.getPhoto() : null)
            .localisation(u != null ? u.getLocalHabitat() : null)
            .rolePosteur(u != null ? u.getRole() : null)
            .typePub(p.getTypePub())
            .idProduit(p.getIdProduit()).nomProduit(nomProduit)
            .imageProduit(imageProduit).quantiteOfferte(p.getQuantiteOfferte())
            .prixOffre(p.getPrixOffre()).uniteMesure(p.getUniteMesure())
            .disponible(p.getDisponible()).poidsUnitaire(poidsUnitaire)
            .build();
    }

    private CommentaireDTO toCommentaireDTO(Commentaire c) {
        Utilisateur u = c.getAuteur() != null ? c.getAuteur()
            : userRepo.findById(c.getIdUtilisateur()).orElse(null);
        return CommentaireDTO.builder()
            .id(c.getId()).contenu(c.getContenu())
            .dateCommentaire(c.getDateCommentaire())
            .idAuteur(c.getIdUtilisateur())
            .nomAuteur(u != null ? u.getPrenom() + " " + u.getNom() : "Utilisateur")
            .photoAuteur(u != null ? u.getPhoto() : null)
            .build();
    }
}

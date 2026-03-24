package com.agriconnect.service_offre.controller;

import com.agriconnect.service_offre.service.*;
import com.agriconnect.service_offre.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offres")
@CrossOrigin(origins = "http://localhost:8100")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService pubService;
    private final CommandeService    cmdService;
    private final ProduitService     produitService;

    private Integer id(Authentication a) { return (Integer) a.getDetails(); }

    // ══════════════════════════════════════════════════════════
    // PRODUITS — catalogue du producteur
    // ══════════════════════════════════════════════════════════

    // GET /api/offres/produits
    @GetMapping("/produits")
    public ResponseEntity<List<ProduitDTO>> mesProduits(Authentication auth) {
        return ResponseEntity.ok(produitService.getMesProduits(id(auth)));
    }

    // POST /api/offres/produits
    // imageUrl = URL de l'image uploadée par le frontend
    @PostMapping("/produits")
    public ResponseEntity<ProduitDTO> creerProduit(
            Authentication auth,
            @RequestParam                 String    nom,
            @RequestParam(required=false) String    descript,
            @RequestParam(required=false) BigDecimal prixUnitaire,
            @RequestParam(required=false) BigDecimal poidsUnitaire,
            @RequestParam(required=false) Integer   stockDisponible,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
                                          LocalDate datePeremption,
            @RequestParam(required=false) String    imageUrl) {
        return ResponseEntity.ok(produitService.creer(
            id(auth), nom, descript, prixUnitaire, poidsUnitaire,
            stockDisponible, datePeremption, imageUrl));
    }

    // PUT /api/offres/produits/{id}
    @PutMapping("/produits/{id}")
    public ResponseEntity<ProduitDTO> modifierProduit(
            Authentication auth,
            @PathVariable Integer id,
            @RequestParam(required=false) String    nom,
            @RequestParam(required=false) String    descript,
            @RequestParam(required=false) BigDecimal prixUnitaire,
            @RequestParam(required=false) BigDecimal poidsUnitaire,
            @RequestParam(required=false) Integer   stockDisponible,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
                                          LocalDate datePeremption,
            @RequestParam(required=false) String    imageUrl) {
        return ResponseEntity.ok(produitService.modifier(
            id, id(auth), nom, descript, prixUnitaire, poidsUnitaire,
            stockDisponible, datePeremption, imageUrl));
    }

    // PATCH /api/offres/produits/{id}/stock
    @PatchMapping("/produits/{id}/stock")
    public ResponseEntity<ProduitDTO> majStock(
            Authentication auth, @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(produitService.majStock(id, id(auth), body.get("stock")));
    }

    // DELETE /api/offres/produits/{id}
    @DeleteMapping("/produits/{id}")
    public ResponseEntity<?> supprimerProduit(Authentication auth, @PathVariable Integer id) {
        produitService.supprimer(id, id(auth));
        return ResponseEntity.ok(Map.of("message", "Produit supprimé"));
    }

    // ══════════════════════════════════════════════════════════
    // PUBLICATIONS
    // ══════════════════════════════════════════════════════════

    // GET /api/offres/feed?page=0&size=10&type=ALL|POST|OFFRE|RECHERCHE
    @GetMapping("/feed")
    public ResponseEntity<Page<PublicationDTO>> feed(
            Authentication auth,
            @RequestParam(defaultValue="0")   int    page,
            @RequestParam(defaultValue="10")  int    size,
            @RequestParam(defaultValue="ALL") String type) {
        if ("ALL".equals(type))
            return ResponseEntity.ok(pubService.getFeed(id(auth), page, size));
        return ResponseEntity.ok(pubService.getFeedParType(type, id(auth), page, size));
    }

    // GET /api/offres/user/{idUser}
    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<PublicationDTO>> byUser(
            Authentication auth, @PathVariable Integer idUser) {
        return ResponseEntity.ok(pubService.getByUser(idUser, id(auth)));
    }

    // POST /api/offres
    // imageUrl = URL de l'image uploadée par le frontend
    @PostMapping
    public ResponseEntity<PublicationDTO> creer(
            Authentication auth,
            @RequestParam(required=false) String     contenu,
            @RequestParam(required=false) String     typePub,
            @RequestParam(required=false) Integer    idProduit,
            @RequestParam(required=false) Integer    quantiteOfferte,
            @RequestParam(required=false) BigDecimal prixOffre,
            @RequestParam(required=false) String     uniteMesure,
            @RequestParam(required=false) String     imageUrl) {
        return ResponseEntity.ok(pubService.creer(
            id(auth), contenu, typePub, idProduit,
            quantiteOfferte, prixOffre, uniteMesure, imageUrl));
    }

    // DELETE /api/offres/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(Authentication auth, @PathVariable Integer id) {
        pubService.supprimer(id, id(auth));
        return ResponseEntity.ok(Map.of("message", "Supprimée"));
    }

    // PUT /api/offres/{id}/indisponible
    @PutMapping("/{id}/indisponible")
    public ResponseEntity<?> indisponible(Authentication auth, @PathVariable Integer id) {
        pubService.marquerIndisponible(id, id(auth));
        return ResponseEntity.ok(Map.of("message", "Offre désactivée"));
    }

    // POST /api/offres/{id}/like
    @PostMapping("/{id}/like")
    public ResponseEntity<PublicationDTO> like(Authentication auth, @PathVariable Integer id) {
        return ResponseEntity.ok(pubService.toggleLike(id, id(auth)));
    }

    // GET /api/offres/{id}/commentaires
    @GetMapping("/{id}/commentaires")
    public ResponseEntity<List<CommentaireDTO>> commentaires(@PathVariable Integer id) {
        return ResponseEntity.ok(pubService.getCommentaires(id));
    }

    // POST /api/offres/{id}/commentaires
    @PostMapping("/{id}/commentaires")
    public ResponseEntity<CommentaireDTO> commenter(
            Authentication auth, @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(pubService.commenter(id, id(auth), body.get("contenu")));
    }

    // GET /api/offres/recherche?q=tomates&type=OFFRE
    @GetMapping("/recherche")
    public ResponseEntity<List<PublicationDTO>> rechercher(
            Authentication auth,
            @RequestParam                 String q,
            @RequestParam(required=false) String type) {
        return ResponseEntity.ok(pubService.rechercher(q, type, id(auth)));
    }

    // ══════════════════════════════════════════════════════════
    // COMMANDES
    // ══════════════════════════════════════════════════════════

    // POST /api/offres/{id}/commander
    @PostMapping("/{id}/commander")
    public ResponseEntity<CommandeDTO> commander(
            Authentication auth, @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        Integer quantite = (Integer) body.get("quantite");
        String methode   = (String) body.getOrDefault("methodePaiement", "PAIEMENT_LIVRAISON");
        return ResponseEntity.ok(cmdService.commander(id, id(auth), quantite, methode));
    }

    // PUT /api/offres/commandes/{id}/accepter
    @PutMapping("/commandes/{id}/accepter")
    public ResponseEntity<CommandeDTO> accepter(Authentication auth, @PathVariable Integer id) {
        return ResponseEntity.ok(cmdService.accepterCommande(id, id(auth)));
    }

    // PUT /api/offres/commandes/{id}/refuser
    @PutMapping("/commandes/{id}/refuser")
    public ResponseEntity<CommandeDTO> refuser(Authentication auth, @PathVariable Integer id) {
        return ResponseEntity.ok(cmdService.refuserCommande(id, id(auth)));
    }

    // PUT /api/offres/commandes/{id}/livrer
    @PutMapping("/commandes/{id}/livrer")
    public ResponseEntity<CommandeDTO> livrer(Authentication auth, @PathVariable Integer id) {
        return ResponseEntity.ok(cmdService.marquerLivre(id, id(auth)));
    }

    // PUT /api/offres/commandes/{id}/annuler
    @PutMapping("/commandes/{id}/annuler")
    public ResponseEntity<CommandeDTO> annuler(Authentication auth, @PathVariable Integer id) {
        return ResponseEntity.ok(cmdService.annuler(id, id(auth)));
    }

    // GET /api/offres/commandes/mes-commandes
    @GetMapping("/commandes/mes-commandes")
    public ResponseEntity<List<CommandeDTO>> mesCommandes(Authentication auth) {
        return ResponseEntity.ok(cmdService.getMesCommandes(id(auth)));
    }

    // GET /api/offres/commandes/mes-ventes
    @GetMapping("/commandes/mes-ventes")
    public ResponseEntity<List<CommandeDTO>> mesVentes(Authentication auth) {
        return ResponseEntity.ok(cmdService.getMesVentes(id(auth)));
    }

    // GET /api/offres/commandes/mes-missions
    @GetMapping("/commandes/mes-missions")
    public ResponseEntity<List<CommandeDTO>> mesMissions(Authentication auth) {
        return ResponseEntity.ok(cmdService.getMesMissions(id(auth)));
    }
}

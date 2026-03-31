package com.agriconnect.service_offre.controller;

import com.agriconnect.service_offre.dto.VehiculeDTO;
import com.agriconnect.service_offre.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offres/vehicules")
@CrossOrigin(origins = {"http://localhost:8100", "http://localhost:4200"})
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    private Integer id(Authentication a) {
        return (Integer) a.getDetails();
    }

    // ── GET /api/offres/vehicules
    // Liste tous mes véhicules
    @GetMapping
    public ResponseEntity<List<VehiculeDTO>> mesVehicules(Authentication auth) {
        return ResponseEntity.ok(vehiculeService.getMesVehicules(id(auth)));
    }

    // ── GET /api/offres/vehicules/disponibles
    // Liste mes véhicules disponibles et pas en mission
    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculeDTO>> mesVehiculesDisponibles(Authentication auth) {
        return ResponseEntity.ok(vehiculeService.getMesVehiculesDisponibles(id(auth)));
    }

    // ── POST /api/offres/vehicules
    // Ajouter un véhicule
    @PostMapping
    public ResponseEntity<VehiculeDTO> ajouter(
            Authentication auth,
            @RequestParam String marque,
            @RequestParam String modele,
            @RequestParam String immatriculation,
            @RequestParam String typeVehicule,
            @RequestParam BigDecimal chargeMaxKg,
            @RequestParam(required = false) BigDecimal volumeMaxM3,
            @RequestParam(required = false) String photoVehicule) {

        return ResponseEntity.ok(vehiculeService.ajouterVehicule(
            id(auth), marque, modele, immatriculation,
            typeVehicule, chargeMaxKg, volumeMaxM3, photoVehicule));
    }

    // ── PUT /api/offres/vehicules/{id}
    // Modifier un véhicule
    @PutMapping("/{id}")
    public ResponseEntity<VehiculeDTO> modifier(
            Authentication auth,
            @PathVariable Integer id,
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) String modele,
            @RequestParam(required = false) String immatriculation,
            @RequestParam(required = false) String typeVehicule,
            @RequestParam(required = false) BigDecimal chargeMaxKg,
            @RequestParam(required = false) BigDecimal volumeMaxM3,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) String photoVehicule) {

        return ResponseEntity.ok(vehiculeService.modifierVehicule(
            id, id(auth), marque, modele, immatriculation,
            typeVehicule, chargeMaxKg, volumeMaxM3, disponible, photoVehicule));
    }

    // ── DELETE /api/offres/vehicules/{id}
    // Supprimer un véhicule
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(Authentication auth, @PathVariable Integer id) {
        vehiculeService.supprimerVehicule(id, id(auth));
        return ResponseEntity.ok(Map.of("message", "Véhicule supprimé"));
    }
}
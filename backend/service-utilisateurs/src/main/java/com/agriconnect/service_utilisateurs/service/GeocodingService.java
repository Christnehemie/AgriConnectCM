package com.agriconnect.service_utilisateurs.service;

import com.agriconnect.service_utilisateurs.dto.CoordinatesDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // API Nominatim (OpenStreetMap) - GRATUITE ET TRÈS PRÉCISE
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";
    
    @Value("${geocoding.api.key:}")
    private String apiKey;
    
    @Value("${geocoding.provider:nominatim}")
    private String provider;
    
    // User-Agent requis par Nominatim (obligatoire)
    private static final String USER_AGENT = "AgriConnect/1.0 (contact@agriconnect.com)";

    // ============================================================
    // GÉOCODIFICATION AVANCÉE
    // ============================================================
    
    /**
     * Convertit une adresse en coordonnées GPS
     * Supporte tous les niveaux de précision (pays, région, ville, quartier, rue)
     */
    public CoordinatesDTO geocode(String address) {
        if (address == null || address.isBlank()) {
            return CoordinatesDTO.error("Adresse vide");
        }
        
        // 1. Vérifier si c'est déjà des coordonnées GPS
        CoordinatesDTO alreadyCoordinates = parseCoordinatesFromString(address);
        if (alreadyCoordinates != null && alreadyCoordinates.isSuccess()) {
            log.info("📍 Adresse déjà au format GPS: {}", address);
            return alreadyCoordinates;
        }
        
        // 2. Nettoyer et enrichir l'adresse
        String enrichedAddress = enrichAddress(address);
        log.debug("🔍 Adresse enrichie: {}", enrichedAddress);
        
        // 3. Appeler l'API de géocodification
        try {
            CoordinatesDTO result = geocodeWithNominatim(enrichedAddress);
            
            if (result.isSuccess()) {
                log.info("✅ Géocodification réussie: '{}' → ({}, {})", 
                    address, result.getLatitude(), result.getLongitude());
                log.debug("📍 Adresse formatée: {}", result.getFormattedAddress());
                log.debug("🏙️ Ville: {}, Pays: {}", result.getCity(), result.getCountry());
            } else {
                log.warn("⚠️ Échec géocodification pour: {}", address);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la géocodification: {}", e.getMessage());
            return CoordinatesDTO.error("Erreur technique: " + e.getMessage());
        }
    }
    
    /**
     * Géocodification avec Nominatim - Supporte tous les niveaux de précision
     */
    private CoordinatesDTO geocodeWithNominatim(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                .queryParam("q", address)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .queryParam("addressdetails", 1)
                .queryParam("extratags", 1)
                .queryParam("namedetails", 1)
                .build()
                .toUriString();
            
            // ✅ CORRIGÉ : renommer en requestEntity pour éviter le conflit
            var requestEntity = org.springframework.http.HttpEntity.EMPTY;
            String response = restTemplate.execute(
                url,
                org.springframework.http.HttpMethod.GET,
                request -> {
                    request.getHeaders().set("User-Agent", USER_AGENT);
                },
                clientHttpResponse -> {
                    return new String(clientHttpResponse.getBody().readAllBytes());
                }
            );
            
            JsonNode root = objectMapper.readTree(response);
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                
                BigDecimal lat = new BigDecimal(first.get("lat").asText())
                    .setScale(8, RoundingMode.HALF_UP);
                BigDecimal lon = new BigDecimal(first.get("lon").asText())
                    .setScale(8, RoundingMode.HALF_UP);
                String displayName = first.get("display_name").asText();
                
                // Extraire les détails d'adresse
                JsonNode addressDetails = first.get("address");
                String city = extractCity(addressDetails);
                String country = extractCountry(addressDetails);
                String neighbourhood = extractNeighbourhood(addressDetails);
                String road = extractRoad(addressDetails);
                String suburb = extractSuburb(addressDetails);
                
                // Déterminer le niveau de précision
                String precision = determinePrecision(addressDetails);
                
                return CoordinatesDTO.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .formattedAddress(displayName)
                    .city(city)
                    .country(country)
                    .neighbourhood(neighbourhood)
                    .road(road)
                    .suburb(suburb)
                    .precision(precision)
                    .success(true)
                    .build();
            }
            
            return CoordinatesDTO.error("Aucun résultat trouvé pour: " + address);
            
        } catch (Exception e) {
            log.error("Erreur Nominatim: {}", e.getMessage());
            return CoordinatesDTO.error("Erreur API: " + e.getMessage());
        }
    }
    
    /**
     * Géocodification inversée (coordonnées → adresse)
     */
    public CoordinatesDTO reverseGeocode(BigDecimal lat, BigDecimal lon) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_REVERSE_URL)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("format", "json")
                .queryParam("addressdetails", 1)
                .build()
                .toUriString();
            
            // ✅ CORRIGÉ : renommer en requestEntity pour éviter le conflit
            var requestEntity = org.springframework.http.HttpEntity.EMPTY;
            String response = restTemplate.execute(
                url,
                org.springframework.http.HttpMethod.GET,
                request -> {
                    request.getHeaders().set("User-Agent", USER_AGENT);
                },
                clientHttpResponse -> {
                    return new String(clientHttpResponse.getBody().readAllBytes());
                }
            );
            
            JsonNode root = objectMapper.readTree(response);
            if (root.has("display_name")) {
                String displayName = root.get("display_name").asText();
                JsonNode addressDetails = root.get("address");
                
                String city = extractCity(addressDetails);
                String country = extractCountry(addressDetails);
                String neighbourhood = extractNeighbourhood(addressDetails);
                String road = extractRoad(addressDetails);
                String suburb = extractSuburb(addressDetails);
                
                return CoordinatesDTO.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .formattedAddress(displayName)
                    .city(city)
                    .country(country)
                    .neighbourhood(neighbourhood)
                    .road(road)
                    .suburb(suburb)
                    .success(true)
                    .build();
            }
            
            return CoordinatesDTO.error("Aucune adresse trouvée");
            
        } catch (Exception e) {
            return CoordinatesDTO.error("Erreur: " + e.getMessage());
        }
    }
    
    // ============================================================
    // MÉTHODES D'EXTRACTION DÉTAILLÉES
    // ============================================================
    
    private String extractCity(JsonNode address) {
        if (address == null) return null;
        if (address.has("city")) return address.get("city").asText();
        if (address.has("town")) return address.get("town").asText();
        if (address.has("village")) return address.get("village").asText();
        if (address.has("municipality")) return address.get("municipality").asText();
        if (address.has("county")) return address.get("county").asText();
        return null;
    }
    
    private String extractCountry(JsonNode address) {
        if (address == null) return null;
        if (address.has("country")) return address.get("country").asText();
        return null;
    }
    
    private String extractNeighbourhood(JsonNode address) {
        if (address == null) return null;
        if (address.has("neighbourhood")) return address.get("neighbourhood").asText();
        if (address.has("suburb")) return address.get("suburb").asText();
        if (address.has("hamlet")) return address.get("hamlet").asText();
        return null;
    }
    
    private String extractRoad(JsonNode address) {
        if (address == null) return null;
        if (address.has("road")) return address.get("road").asText();
        return null;
    }
    
    private String extractSuburb(JsonNode address) {
        if (address == null) return null;
        if (address.has("suburb")) return address.get("suburb").asText();
        return null;
    }
    
    private String determinePrecision(JsonNode address) {
        if (address == null) return "pays";
        if (address.has("road")) return "rue";
        if (address.has("neighbourhood") || address.has("suburb")) return "quartier";
        if (address.has("city") || address.has("town")) return "ville";
        if (address.has("county")) return "departement";
        if (address.has("state")) return "region";
        return "pays";
    }
    
    // ============================================================
    // ENRICHISSEMENT D'ADRESSE
    // ============================================================
    
    /**
     * Enrichit l'adresse pour améliorer la géocodification
     */
    private String enrichAddress(String address) {
        String enriched = address;
        
        // Ajouter "Cameroun" si absent
        if (!enriched.toLowerCase().contains("cameroun") && 
            !enriched.toLowerCase().contains("cameroon")) {
            enriched = enriched + ", Cameroun";
        }
        
        // Nettoyer les caractères spéciaux
        enriched = enriched.replaceAll("[\\s,]+$", "");
        
        return enriched;
    }
    
    // ============================================================
    // DÉTECTION DE FORMATS SPÉCIFIQUES
    // ============================================================
    
    /**
     * Détecte si l'adresse est un quartier connu au Cameroun
     */
    private boolean isKnownNeighbourhood(String address) {
        String[] knownNeighbourhoods = {
            "Djeleng", "Djeleng Bafoussam", "Quartier Djeleng",
            "Melen", "Melen Yaoundé", "Quartier Melen",
            "Mvog-Mbi", "Mvog-Mbi Yaoundé",
            "Bonapriso", "Bonapriso Douala",
            "Akwa", "Akwa Douala",
            "Deido", "Deido Douala",
            "Bali", "Bali Bafoussam",
            "Mifi", "Mifi Bafoussam"
        };
        
        for (String n : knownNeighbourhoods) {
            if (address.toLowerCase().contains(n.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si la chaîne est déjà au format GPS
     */
    private CoordinatesDTO parseCoordinatesFromString(String input) {
        try {
            String cleaned = input.replaceAll("[\\s,;]+", " ").trim();
            String[] parts = cleaned.split(" ");
            
            if (parts.length >= 2) {
                BigDecimal lat = new BigDecimal(parts[0]);
                BigDecimal lon = new BigDecimal(parts[1]);
                
                if (lat.compareTo(new BigDecimal("-90")) >= 0 && 
                    lat.compareTo(new BigDecimal("90")) <= 0 &&
                    lon.compareTo(new BigDecimal("-180")) >= 0 && 
                    lon.compareTo(new BigDecimal("180")) <= 0) {
                    
                    return CoordinatesDTO.success(lat, lon, input);
                }
            }
        } catch (NumberFormatException e) {
            // Pas un format GPS
        }
        return null;
    }
}
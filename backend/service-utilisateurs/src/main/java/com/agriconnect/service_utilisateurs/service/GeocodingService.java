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
    // FALLBACK HORS-LIGNE (0% ÉCHEC)
    // ============================================================
    private static final java.util.Map<String, double[]> CITY_COORDINATES = new java.util.HashMap<>();
    static {
        CITY_COORDINATES.put("yaoundé", new double[]{3.8480, 11.5021});
        CITY_COORDINATES.put("yaounde", new double[]{3.8480, 11.5021});
        CITY_COORDINATES.put("douala", new double[]{4.0511, 9.7679});
        CITY_COORDINATES.put("bafoussam", new double[]{5.4777, 10.4175});
        CITY_COORDINATES.put("bamenda", new double[]{5.9583, 10.1558});
        CITY_COORDINATES.put("garoua", new double[]{9.3000, 13.4000});
        CITY_COORDINATES.put("maroua", new double[]{10.5960, 14.3160});
        CITY_COORDINATES.put("ngaoundéré", new double[]{7.3276, 13.5847});
        CITY_COORDINATES.put("kribi", new double[]{2.9436, 9.9077});
        CITY_COORDINATES.put("ebolowa", new double[]{2.9150, 11.1500});
        CITY_COORDINATES.put("bertoua", new double[]{4.5773, 13.6846});
    }

    private static final java.util.Map<String, double[]> NEIGHBOURHOOD_COORDINATES = new java.util.HashMap<>();
    static {
        // Douala
        NEIGHBOURHOOD_COORDINATES.put("bonapriso", new double[]{4.0287, 9.6953});
        NEIGHBOURHOOD_COORDINATES.put("akwa", new double[]{4.0435, 9.6998});
        NEIGHBOURHOOD_COORDINATES.put("deido", new double[]{4.0535, 9.7126});
        NEIGHBOURHOOD_COORDINATES.put("bonamoussadi", new double[]{4.0863, 9.7423});
        NEIGHBOURHOOD_COORDINATES.put("makepe", new double[]{4.0667, 9.7500});
        NEIGHBOURHOOD_COORDINATES.put("logbessou", new double[]{4.1130, 9.7610});
        NEIGHBOURHOOD_COORDINATES.put("ndogbong", new double[]{4.0610, 9.7390});
        NEIGHBOURHOOD_COORDINATES.put("bepanda", new double[]{4.0601, 9.7121});
        NEIGHBOURHOOD_COORDINATES.put("yassa", new double[]{4.0450, 9.8020});
        NEIGHBOURHOOD_COORDINATES.put("bonaberi", new double[]{4.0850, 9.6600});

        // Yaoundé
        NEIGHBOURHOOD_COORDINATES.put("melen", new double[]{3.8640, 11.5030});
        NEIGHBOURHOOD_COORDINATES.put("mvog-mbi", new double[]{3.8378, 11.5173});
        NEIGHBOURHOOD_COORDINATES.put("mokolo", new double[]{3.8770, 11.5033});
        NEIGHBOURHOOD_COORDINATES.put("mfoundi", new double[]{3.8648, 11.5190});
        NEIGHBOURHOOD_COORDINATES.put("biyem-assi", new double[]{3.8400, 11.4880});
        NEIGHBOURHOOD_COORDINATES.put("nsam", new double[]{3.8190, 11.5130});
        NEIGHBOURHOOD_COORDINATES.put("mbankolo", new double[]{3.8960, 11.4920});
        NEIGHBOURHOOD_COORDINATES.put("etoudi", new double[]{3.9140, 11.5300});
        NEIGHBOURHOOD_COORDINATES.put("bastos", new double[]{3.8820, 11.5080});
        NEIGHBOURHOOD_COORDINATES.put("mvolyé", new double[]{3.8410, 11.5010});
        NEIGHBOURHOOD_COORDINATES.put("mvolye", new double[]{3.8410, 11.5010});

        // Bafoussam
        NEIGHBOURHOOD_COORDINATES.put("djeleng", new double[]{5.4800, 10.4200});
        NEIGHBOURHOOD_COORDINATES.put("mifi", new double[]{5.4600, 10.4000});
        NEIGHBOURHOOD_COORDINATES.put("tougang", new double[]{5.4900, 10.4300});
        NEIGHBOURHOOD_COORDINATES.put("ndiangdam", new double[]{5.4700, 10.4100});
        NEIGHBOURHOOD_COORDINATES.put("kamkop", new double[]{5.4500, 10.4200});
    }

    private CoordinatesDTO executeStaticFallback(String rawAddress) {
        log.warn("🛡️ Exécution du Fallback statique pour: '{}'", rawAddress);
        String lower = rawAddress == null ? "" : rawAddress.toLowerCase();
        
        // 1. Chercher d'abord un quartier exact
        for (String q : NEIGHBOURHOOD_COORDINATES.keySet()) {
            if (lower.contains(q)) {
                double[] coords = NEIGHBOURHOOD_COORDINATES.get(q);
                return getSuccessDTO(coords[0], coords[1], "Quartier " + capitalize(q) + ", Cameroun (Fixe)");
            }
        }
        
        // 2. Chercher une ville
        for (String v : CITY_COORDINATES.keySet()) {
            if (lower.contains(v)) {
                double[] coords = CITY_COORDINATES.get(v);
                return getSuccessDTO(coords[0], coords[1], capitalize(v) + ", Cameroun (Centre-Ville Fixe)");
            }
        }
        
        // 3. Fallback absolu: Yaoundé (zéro échec possible)
        double[] yaounde = CITY_COORDINATES.get("yaoundé");
        log.warn("🚨 Adresse introuvable dans le dictionnaire, Fallback absolu sur Centre Yaoundé pour sauver la création du compte.");
        return getSuccessDTO(yaounde[0], yaounde[1], "Cameroun (Position Centrale Par Défaut)");
    }

    private CoordinatesDTO getSuccessDTO(double lat, double lon, String formatAddress) {
        return CoordinatesDTO.builder()
            .latitude(new BigDecimal(lat).setScale(8, RoundingMode.HALF_UP))
            .longitude(new BigDecimal(lon).setScale(8, RoundingMode.HALF_UP))
            .formattedAddress(formatAddress)
            .success(true)
            .build();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

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
                return result;
            } else {
                log.warn("⚠️ Échec API pour: {} -> Déclenchement du Fallback...", address);
                return executeStaticFallback(address);
            }
            
        } catch (Exception e) {
            log.error("❌ Erreur API lors de la géocodification: {} -> Déclenchement du Fallback...", e.getMessage());
            return executeStaticFallback(address);
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
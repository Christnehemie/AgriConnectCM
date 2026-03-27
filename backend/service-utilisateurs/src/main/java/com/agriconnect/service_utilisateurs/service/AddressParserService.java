package com.agriconnect.service_utilisateurs.service;

import com.agriconnect.service_utilisateurs.dto.CoordinatesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressParserService {

    private final GeocodingService geocodingService;
    
    // Patterns pour détecter différents formats d'adresse
    private static final Pattern GPS_PATTERN = 
        Pattern.compile("^(-?\\d+\\.?\\d*)[,\\s]+(-?\\d+\\.?\\d*)$");
    
    // Patterns pour détecter les quartiers (ex: "Quartier Djeleng", "Djeleng")
    private static final Pattern NEIGHBOURHOOD_PATTERN = 
        Pattern.compile("(?i)(quartier\\s+)?([A-Za-zÀ-ÿ]+)", Pattern.UNICODE_CHARACTER_CLASS);
    
    // Villes principales du Cameroun
    private static final Map<String, String> CAMEROON_CITIES = new HashMap<>();
    static {
        CAMEROON_CITIES.put("yaoundé", "Yaoundé, Centre, Cameroun");
        CAMEROON_CITIES.put("douala", "Douala, Littoral, Cameroun");
        CAMEROON_CITIES.put("bafoussam", "Bafoussam, Ouest, Cameroun");
        CAMEROON_CITIES.put("bamenda", "Bamenda, Nord-Ouest, Cameroun");
        CAMEROON_CITIES.put("garoua", "Garoua, Nord, Cameroun");
        CAMEROON_CITIES.put("maroua", "Maroua, Extrême-Nord, Cameroun");
        CAMEROON_CITIES.put("ngaoundéré", "Ngaoundéré, Adamaoua, Cameroun");
        CAMEROON_CITIES.put("kribi", "Kribi, Sud, Cameroun");
        CAMEROON_CITIES.put("ebolowa", "Ebolowa, Sud, Cameroun");
        CAMEROON_CITIES.put("bertoua", "Bertoua, Est, Cameroun");
    }
    
    // Quartiers connus par ville
    private static final Map<String, String[]> KNOWN_NEIGHBOURHOODS = new HashMap<>();
    static {
        KNOWN_NEIGHBOURHOODS.put("yaoundé", new String[]{
            "Melen", "Mvog-Mbi", "Mokolo", "Mfoundi", "Biyem-Assi", 
            "Nsam", "Mbankolo", "Etoudi", "Bastos", "Mvolyé"
        });
        KNOWN_NEIGHBOURHOODS.put("douala", new String[]{
            "Bonapriso", "Akwa", "Deido", "Bonamoussadi", "Makepe", 
            "Logbessou", "Ndogbong", "Bepanda", "Yassa", "Bonaberi"
        });
        KNOWN_NEIGHBOURHOODS.put("bafoussam", new String[]{
            "Djeleng", "Mifi", "Tougang", "Ndiangdam", "Kamkop"
        });
    }
    
    /**
     * Parse et géocode une adresse avec tous les niveaux de précision
     */
    public CoordinatesDTO parseAndGeocode(String rawAddress, String city, String country) {
        
        // 1. Construire l'adresse complète
        String fullAddress = buildFullAddress(rawAddress, city, country);
        log.info("🔍 Adresse complète à géocoder: {}", fullAddress);
        
        // 2. Appeler le service de géocodification
        CoordinatesDTO result = geocodingService.geocode(fullAddress);
        
        if (result.isSuccess()) {
            log.info("✅ Géocodification réussie:");
            log.info("   - Adresse: {}", result.getFormattedAddress());
            log.info("   - Ville: {}", result.getCity());
            log.info("   - Quartier: {}", result.getNeighbourhood());
            log.info("   - Rue: {}", result.getRoad());
            log.info("   - Précision: {}", result.getPrecision());
        }
        
        return result;
    }
    
    /**
     * Construit une adresse complète à partir des composants
     */
    public String buildFullAddress(String rawAddress, String city, String country) {
        StringBuilder fullAddress = new StringBuilder();
        
        // Ajouter l'adresse brute (peut contenir quartier, rue)
        if (rawAddress != null && !rawAddress.isBlank()) {
            fullAddress.append(rawAddress.trim());
        }
        
        // Ajouter la ville si spécifiée
        if (city != null && !city.isBlank()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city.trim());
        }
        
        // Ajouter le pays (par défaut Cameroun)
        if (country != null && !country.isBlank()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country.trim());
        } else {
            if (fullAddress.length() > 0) fullAddress.append(", Cameroun");
            else fullAddress.append("Cameroun");
        }
        
        return fullAddress.toString();
    }
    
    /**
     * Détecte si une adresse est un quartier connu
     */
    public boolean isKnownNeighbourhood(String address, String city) {
        if (address == null) return false;
        
        String lowerAddress = address.toLowerCase();
        String lowerCity = city != null ? city.toLowerCase() : "";
        
        // Vérifier dans les quartiers connus de la ville
        if (KNOWN_NEIGHBOURHOODS.containsKey(lowerCity)) {
            for (String n : KNOWN_NEIGHBOURHOODS.get(lowerCity)) {
                if (lowerAddress.contains(n.toLowerCase())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Extrait le nom du quartier d'une adresse
     */
    public String extractNeighbourhood(String address) {
        if (address == null) return null;
        
        // Pattern pour détecter "Quartier X" ou simplement un nom de quartier
        Matcher m = Pattern.compile("(?i)(quartier\\s+)?([A-Za-zÀ-ÿ]+)").matcher(address);
        if (m.find()) {
            String possible = m.group(2);
            if (possible.length() > 3) {
                return possible.substring(0, 1).toUpperCase() + possible.substring(1).toLowerCase();
            }
        }
        return null;
    }
    
    /**
     * Suggère une adresse améliorée à partir d'une adresse vague
     */
    public String suggestImprovedAddress(String rawAddress, String city) {
        String lowerAddress = rawAddress.toLowerCase();
        
        // Si c'est juste un nom de quartier, ajouter la ville
        if (isKnownNeighbourhood(rawAddress, city)) {
            return rawAddress + ", " + city + ", Cameroun";
        }
        
        // Si c'est juste un nom de ville, ajouter le pays
        if (CAMEROON_CITIES.containsKey(lowerAddress)) {
            return CAMEROON_CITIES.get(lowerAddress);
        }
        
        return rawAddress;
    }
    
    /**
     * Valide si une adresse a assez d'informations pour être géocodée
     */
    public boolean isValidAddress(String address) {
        if (address == null || address.isBlank()) return false;
        
        // Au moins un des éléments suivants doit être présent
        boolean hasCity = CAMEROON_CITIES.keySet().stream()
            .anyMatch(city -> address.toLowerCase().contains(city));
        boolean hasCountry = address.toLowerCase().contains("cameroun") || 
                             address.toLowerCase().contains("cameroon");
        boolean hasNeighbourhood = isKnownNeighbourhood(address, null);
        
        return hasCity || hasCountry || hasNeighbourhood;
    }
}
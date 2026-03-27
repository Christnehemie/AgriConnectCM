package com.agriconnect.service_utilisateurs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesDTO {
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String formattedAddress;
    private String city;
    private String country;
    private String neighbourhood;    // Quartier
    private String road;              // Rue
    private String suburb;            // Banlieue/quartier
    private String precision;         // pays, region, departement, ville, quartier, rue
    private boolean success;
    private String errorMessage;
    
    public static CoordinatesDTO error(String message) {
        return CoordinatesDTO.builder()
            .success(false)
            .errorMessage(message)
            .build();
    }
    
    public static CoordinatesDTO success(BigDecimal lat, BigDecimal lon, String address) {
        return CoordinatesDTO.builder()
            .latitude(lat)
            .longitude(lon)
            .formattedAddress(address)
            .success(true)
            .build();
    }
    
    /**
     * Retourne l'adresse au format lisible
     */
    public String getHumanReadableAddress() {
        if (formattedAddress != null) return formattedAddress;
        
        StringBuilder sb = new StringBuilder();
        if (road != null) sb.append(road);
        if (neighbourhood != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(neighbourhood);
        }
        if (city != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (country != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }
    
    /**
     * Retourne un résumé de la localisation
     */
    public String getLocationSummary() {
        if (neighbourhood != null) {
            return neighbourhood + ", " + (city != null ? city : "");
        }
        if (city != null) {
            return city;
        }
        return formattedAddress != null ? formattedAddress : "Localisation inconnue";
    }
}
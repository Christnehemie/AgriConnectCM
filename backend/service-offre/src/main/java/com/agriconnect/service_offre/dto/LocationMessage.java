package com.agriconnect.service_offre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessage {
    private Integer idUtilisateur;
    private String nomUtilisateur;
    private String photoUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime lastUpdated;
    private boolean isOnline;
}

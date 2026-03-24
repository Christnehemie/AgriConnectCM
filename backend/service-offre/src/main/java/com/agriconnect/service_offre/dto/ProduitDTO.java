package com.agriconnect.service_offre.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
@Data 
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class ProduitDTO {
    private Integer id;
    private String nom;
    private String descript;
    private String image;
    private BigDecimal prixUnitaire;
    private BigDecimal poidsUnitaire;
    private Integer stockDisponible;
    private LocalDate datePeremption;
    private Integer idProducteur;
}

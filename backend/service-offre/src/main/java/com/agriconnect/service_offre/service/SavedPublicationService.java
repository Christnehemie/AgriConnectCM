package com.agriconnect.service_offre.service;

import com.agriconnect.service_offre.dto.PublicationDTO;
import com.agriconnect.service_offre.model.SavedPublication;
import com.agriconnect.service_offre.repository.SavedPublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedPublicationService {

    private final SavedPublicationRepository savedRepo;
    private final PublicationService pubService;

    @Transactional
    public void toggleSave(Integer idPublication, Integer idUtilisateur) {
        if (savedRepo.existsByIdPublicationAndIdUtilisateur(idPublication, idUtilisateur)) {
            savedRepo.deleteByIdPublicationAndIdUtilisateur(idPublication, idUtilisateur);
        } else {
            SavedPublication saved = SavedPublication.builder()
                    .idPublication(idPublication)
                    .idUtilisateur(idUtilisateur)
                    .build();
            savedRepo.save(saved);
        }
    }

    public List<PublicationDTO> getSavedPublications(Integer idUtilisateur) {
        List<Integer> ids = savedRepo.findByIdUtilisateur(idUtilisateur).stream()
                .map(SavedPublication::getIdPublication)
                .collect(Collectors.toList());
        
        return pubService.getPublicationsByIds(ids, idUtilisateur);
    }

    public boolean isSavedByMe(Integer idPublication, Integer idUtilisateur) {
        return savedRepo.existsByIdPublicationAndIdUtilisateur(idPublication, idUtilisateur);
    }
}

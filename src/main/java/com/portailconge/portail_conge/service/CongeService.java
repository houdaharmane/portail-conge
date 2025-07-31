package com.portailconge.portail_conge.service;

import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CongeService {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    public int getCongesEnAttente() {
        return demandeCongeRepository.countByStatut(StatutDemande.EN_ATTENTE);
    }

    public int getCongesValides() {
        return demandeCongeRepository.countByStatut(StatutDemande.APPROUVEE);
    }
}

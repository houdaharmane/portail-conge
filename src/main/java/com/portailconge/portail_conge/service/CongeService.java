package com.portailconge.portail_conge.service;

import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CongeService {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    public int getCongesEnAttente() {
        // Compte les demandes avec statut EN_ATTENTE uniquement
        return demandeCongeRepository.countByStatut(StatutDemande.EN_ATTENTE);
    }

    public int getCongesValides() {
        List<StatutDemande> statutsValides = List.of(
                StatutDemande.APPROUVEE_RESP,
                StatutDemande.APPROUVEE_RH);
        return demandeCongeRepository.countByStatutIn(statutsValides);
    }

}

package com.portailconge.portail_conge.service;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemandeService {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    public void save(DemandeConge demande) {
        demandeCongeRepository.save(demande);
    }

    public Page<DemandeConge> getDemandesPage(int page, int size) {
        return demandeCongeRepository.findAll(PageRequest.of(page, size));
    }

    public List<DemandeConge> getDemandesApprouveesParResponsable() {
        return demandeCongeRepository.findByStatut(StatutDemande.APPROUVEE_RESP);
    }

    public List<DemandeConge> getCongesByUtilisateur(Utilisateur utilisateur) {
        return demandeCongeRepository.findByDemandeur(utilisateur);
    }

    public int calculerJoursPrisConfirmes(Utilisateur utilisateur, int annee) {
        List<DemandeConge> demandes = demandeCongeRepository
                .findByDemandeurAndStatutAndAnnee(utilisateur, StatutDemande.APPROUVEE_RESP, annee);

        return demandes.stream()
                .mapToInt(d -> d.getDuree()) // <-- utilisation de getDuree()
                .sum();
    }

    public int calculerSoldeTotalDisponible(Utilisateur utilisateur, int annee) {
        int totalConges = utilisateur.getSoldeConge() != null ? utilisateur.getSoldeConge() : 0;
        int joursPris = calculerJoursPrisConfirmes(utilisateur, annee);
        return totalConges - joursPris;
    }
}

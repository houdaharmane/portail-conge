package com.portailconge.portail_conge.service;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CongeService {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    private static final int CONGES_ANNUELS = 30;

    // Congés en attente
    public int getCongesEnAttente() {
        return demandeCongeRepository.countByStatut(StatutDemande.EN_ATTENTE);
    }

    // Congés validés (directeur + RH)
    public int getCongesValides() {
        List<StatutDemande> statutsValides = List.of(
                StatutDemande.APPROUVEE_DIRECTEUR,
                StatutDemande.APPROUVEE_RH);
        return demandeCongeRepository.countByStatutIn(statutsValides);
    }

    // Congés refusés
    public int getCongesRefuses() {
        return demandeCongeRepository.countByStatut(StatutDemande.REFUSEE_RESP);
    }

    // Calcul des jours pris confirmés pour un utilisateur et une année
    public int calculerJoursPrisConfirmes(Utilisateur utilisateur, int annee) {
        List<StatutDemande> statutsValides = List.of(
                StatutDemande.APPROUVEE_DIRECTEUR);

        Long utilisateurId = Long.valueOf(utilisateur.getId());

        List<DemandeConge> congesApprouves = demandeCongeRepository
                .findByDemandeur_IdAndStatutIn(utilisateurId, statutsValides);

        return congesApprouves.stream()
                .filter(c -> c.getDateDebut() != null && c.getDateDebut().getYear() == annee)
                .mapToInt(DemandeConge::getDuree)
                .sum();
    }

    // Récupérer tous les congés d’un utilisateur
    public List<DemandeConge> getCongesByUtilisateur(Utilisateur utilisateur) {
        return demandeCongeRepository.findByDemandeur(utilisateur);
    }

    // Solde disponible après consommation
    public int calculerSoldeTotalDisponible(Utilisateur utilisateur, int annee) {
        int consommes = calculerJoursPrisConfirmes(utilisateur, annee);
        return Math.max(CONGES_ANNUELS - consommes, 0);
    }
}

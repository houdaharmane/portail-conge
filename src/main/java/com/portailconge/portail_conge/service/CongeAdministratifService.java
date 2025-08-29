package com.portailconge.portail_conge.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.portailconge.portail_conge.model.CongeAdministratif;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.CongeAdministratifRepository;

@Service
public class CongeAdministratifService {

    private final CongeAdministratifRepository congeRepo;

    public CongeAdministratifService(CongeAdministratifRepository congeRepo) {
        this.congeRepo = congeRepo;
    }

    // Calcul du solde total disponible pour une année donnée
    public int calculerSoldeTotalDisponible(Utilisateur utilisateur, int annee) {
        int anneeDebut = 2025; // année de départ

        if (annee < anneeDebut) {
            return 0;
        }

        int soldeTotalPrecedent = 0;

        for (int anneeCourante = anneeDebut; anneeCourante <= annee; anneeCourante++) {
            int congesPris = calculerJoursPrisConfirmes(utilisateur, anneeCourante);
            int soldeAnnee = 30 - congesPris; // solde annuel = 30 - congés pris cette année
            soldeTotalPrecedent += soldeAnnee;
        }

        return Math.max(soldeTotalPrecedent, 0);
    }

    // Retourne le total des jours pris confirmés dans une année
    public int calculerJoursPrisConfirmes(Utilisateur utilisateur, int annee) {
        List<CongeAdministratif> conges = congeRepo.findByUtilisateurAndAnneeAndStatut(
                utilisateur, annee, StatutDemande.APPROUVEE);
        return conges.stream()
                .mapToInt(CongeAdministratif::getJoursPris)
                .sum();
    }

    // Retourne la liste des congés confirmés d'un utilisateur
    public List<CongeAdministratif> getCongesByUtilisateur(Utilisateur utilisateur) {
        return congeRepo.findByUtilisateurAndStatut(utilisateur, StatutDemande.APPROUVEE);
    }
}

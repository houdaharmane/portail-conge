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

    // Méthode principale : calcule le solde total disponible pour une année donnée
    public int calculerSoldeTotalDisponible(Utilisateur utilisateur, int annee) {
        int anneeDebut = 2025; // Choisis ton année de départ (première année de suivi)

        if (annee < anneeDebut) {
            return 0;
        }

        int soldeTotalPrecedent = 0;

        for (int anneeCourante = anneeDebut; anneeCourante <= annee; anneeCourante++) {
            int congesPris = calculerJoursPrisConfirmes(utilisateur, anneeCourante);
            int soldeAnnee = 30 - congesPris; // solde annuel = 30 - congés pris cette année

            // Solde total disponible cumulatif
            soldeTotalPrecedent = soldeTotalPrecedent + soldeAnnee;
        }

        return Math.max(soldeTotalPrecedent, 0);
    }

    // Retourne la somme des jours pris confirmés dans une année (à adapter selon
    // ton modèle)
    public int calculerJoursPrisConfirmes(Utilisateur utilisateur, int annee) {
        // Ici on filtre uniquement les congés confirmés (exemple avec le statut
        // APPROUVEE)
        List<CongeAdministratif> conges = congeRepo.findByUtilisateurAndAnneeAndStatut(utilisateur, annee,
                StatutDemande.APPROUVEE);
        return conges.stream()
                .mapToInt(CongeAdministratif::getJoursPris)
                .sum();
    }

}

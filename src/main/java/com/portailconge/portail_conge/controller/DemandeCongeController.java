package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.DepartementRepository;
import com.portailconge.portail_conge.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/personnel/conges")
public class DemandeCongeController {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @PostMapping("/demande")
    public String enregistrerDemande(
            @RequestParam String matricule,
            @RequestParam int duree,
            @RequestParam @DateTimeFormat(pattern = "d/M/yyyy") LocalDate dateDebut,
            @RequestParam Integer departementId,
            @RequestParam(required = false) String motif,
            Model model) {

        Utilisateur demandeur = utilisateurRepository.findByMatricule(matricule);
        if (demandeur == null) {
            model.addAttribute("error", "Utilisateur non trouvé");
            return "demande-conge-personnel"; // retourner le formulaire avec erreur
        }

        Departement departement = departementRepository.findById(departementId)
                .orElse(null);
        if (departement == null) {
            model.addAttribute("error", "Département invalide");
            return "demande-conge-personnel"; // retourner le formulaire avec erreur
        }

        DemandeConge demande = new DemandeConge();
        demande.setDemandeur(demandeur);
        demande.setDuree(duree);
        demande.setDateDebut(dateDebut);
        demande.setDepartement(departement);
        demande.setStatut(StatutDemande.EN_ATTENTE);

        // Calculer dateFin = dateDebut + duree jours (durée en jours)
        LocalDate dateFin = dateDebut.plusDays(duree);
        demande.setDateFin(dateFin);

        demandeCongeRepository.save(demande);

        model.addAttribute("message", "Demande enregistrée avec succès.");
        return "confirmationDemande"; // page de confirmation
    }

}

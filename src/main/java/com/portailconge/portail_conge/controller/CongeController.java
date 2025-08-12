package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.CongeAdministratif;
import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.CongeAdministratifRepository;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/conge")
public class CongeController {
    @Autowired
    private CongeAdministratifRepository congeAdministratifRepository;
    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @GetMapping("/demande")
    public String afficherFormulaireDemandeConge(Model model) {
        model.addAttribute("activePage", "demandeConge");

        // Exemple statique pour test
        Utilisateur rh = new Utilisateur();
        rh.setRole("RH");
        model.addAttribute("rh", rh);

        return "demande-conge";
    }

    @PostMapping("/etape-suivante")
    public String traiterDemandeConge(@RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            Model model) {
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);

        return "details-demande";
    }

    @GetMapping("/historique-responsable")
    public String afficherDemandesResponsable(Model model) {
        List<DemandeConge> demandes = demandeCongeRepository.findByDemandeurRole("RESPONSABLE");
        model.addAttribute("demandes", demandes);
        return "historique-demandes-responsable";
    }

    @GetMapping("/historique-personnel")
    public String afficherDemandesPersonnel(Model model) {
        List<DemandeConge> demandes = demandeCongeRepository.findByDemandeurRole("PERSONNEL");
        model.addAttribute("demandes", demandes);
        return "historique-demandes-personnel";
    }

    @PostMapping("/demande/{id}/valider")
    public String validerDemande(@PathVariable Long id, Model model) {
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (!demande.getStatut().equals(StatutDemande.EN_ATTENTE) &&
                !demande.getStatut().equals(StatutDemande.EN_ATTENTE_RH)) {
            model.addAttribute("error", "Cette demande a déjà été traitée.");
            return "redirect:/conge/historique-responsable"; // adapter selon ta page
        }

        // Met à jour le statut de la demande
        demande.setStatut(StatutDemande.APPROUVEE);
        demandeCongeRepository.save(demande);

        // Calcul du nombre de jours (inclus)
        long joursPris = ChronoUnit.DAYS.between(demande.getDateDebut(), demande.getDateFin()) + 1;

        // Enregistre dans CongeAdministratif pour déduire du solde
        CongeAdministratif conge = new CongeAdministratif();
        conge.setUtilisateur(demande.getDemandeur());
        conge.setDateDebut(demande.getDateDebut());
        conge.setDateFin(demande.getDateFin());
        conge.setJoursPris((int) joursPris);
        conge.setAnnee(demande.getDateDebut().getYear());

        congeAdministratifRepository.save(conge);

        return "redirect:/conge/historique-responsable";
    }
}

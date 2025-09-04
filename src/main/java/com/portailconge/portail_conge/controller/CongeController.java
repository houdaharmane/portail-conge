package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.CongeAdministratif;
import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.CongeAdministratifRepository;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.UtilisateurRepository;

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
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CongeAdministratifRepository congeAdministratifRepository;

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    /**
     * Affichage du formulaire de demande de congé (RH connecté)
     */
    @GetMapping("/demande")
    public String afficherFormulaireDemandeConge(Model model) {
        model.addAttribute("activePage", "demandeConge");

        // Utilisateur fictif pour test
        Utilisateur rh = utilisateurRepository.findById(1).orElse(null);
        model.addAttribute("rh", rh);
        // Récupérer tous les utilisateurs avec le rôle "RESPONSABLE"
        List<Utilisateur> responsables = utilisateurRepository.findAllByRoleIgnoreCase("RESPONSABLE");
        model.addAttribute("responsables", responsables);

        return "demande-conge";
    }

    /**
     * Étape suivante : réception des dates choisies
     */
    @PostMapping("/etape-suivante")
    public String traiterDemandeConge(@RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            Model model) {
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        return "details-demande";
    }

    /**
     * Historique des demandes pour les responsables
     */
    @GetMapping("/historique-responsable")
    public String afficherDemandesResponsable(Model model) {
        List<DemandeConge> demandes = demandeCongeRepository.findByDemandeurRole("RESPONSABLE");
        model.addAttribute("demandes", demandes);
        return "historique-demandes-responsable";
    }

    /**
     * Historique des demandes pour les personnels
     */
    @GetMapping("/historique-personnel")
    public String afficherDemandesPersonnel(Model model) {
        List<DemandeConge> demandes = demandeCongeRepository.findByDemandeurRole("PERSONNEL");
        model.addAttribute("demandes", demandes);
        return "historique-demandes-personnel";
    }

    /**
     * Validation d’une demande de congé
     */
    @PostMapping("/demande/{id}/valider")
    public String validerDemande(@PathVariable Long id, Model model) {
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (!(demande.getStatut().equals(StatutDemande.EN_ATTENTE) ||
                demande.getStatut().equals(StatutDemande.EN_ATTENTE_RH))) {
            model.addAttribute("error", "Cette demande a déjà été traitée.");
            return "redirect:/conge/historique-responsable";
        }

        // Mise à jour du statut
        demande.setStatut(StatutDemande.APPROUVEE);
        demandeCongeRepository.save(demande);

        // Calcul du nombre de jours
        long joursPris = ChronoUnit.DAYS.between(demande.getDateDebut(), demande.getDateFin()) + 1;

        // Enregistrement dans les congés administratifs
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

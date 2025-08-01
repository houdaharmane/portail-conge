package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.service.CongeService;
import com.portailconge.portail_conge.service.UtilisateurService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ResponsableController {

    @Autowired
    private CongeService congeService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    // Dashboard responsable
    @GetMapping("/dashboard-responsable")
    public String dashboardResponsable(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null) {
            return "redirect:/login";
        }

        if (!("RESPONSABLE".equals(responsable.getRole()) || "PERSONNEL".equals(responsable.getRole()))) {
            return "redirect:/login";
        }

        int nombrePersonnels = utilisateurService.getNombrePersonnels();
        int congesEnAttente = congeService.getCongesEnAttente();
        int congesValides = congeService.getCongesValides();

        model.addAttribute("totalPersonnel", nombrePersonnels);
        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValides", congesValides);
        model.addAttribute("utilisateur", responsable);

        return "dashboard-responsable";
    }

    // Soumission demande (personnel)
    @PostMapping("/conge/demande/soumettre")
    public String soumettreDemande(@ModelAttribute DemandeConge demandeConge, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        demandeConge.setDemandeur(utilisateur);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE);
        demandeConge.setDateSoumission(LocalDate.now());
        demandeCongeRepository.save(demandeConge);

        return "redirect:/conges/confirmation";
    }

    // Afficher demandes en attente pour responsable (statut EN_ATTENTE)
    @GetMapping("/demandes-a-traiter")
    public String afficherDemandesATraiter(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        Departement departement = responsable.getDepartement();
        List<DemandeConge> demandes = demandeCongeRepository.findDemandesEnAttenteByDepartement(
                departement, StatutDemande.EN_ATTENTE);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DemandeConge demande : demandes) {
            if (demande.getDateDebut() != null) {
                demande.setDateDebutFormatee(demande.getDateDebut().format(formatter));
            }
            if (demande.getDateFin() != null) {
                demande.setDateFinFormatee(demande.getDateFin().format(formatter));
            }
        }

        model.addAttribute("demandes", demandes);
        model.addAttribute("activePage", "demandesATraiter");
        return "demandesATraiter";
    }

    // Approuver demande responsable → statut APPROUVEE_RESP
    @GetMapping("/approuver/{id}")
    public String approuverDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.APPROUVEE_RESP);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Refuser demande responsable → statut REFUSEE_RESP
    @GetMapping("/rejeter/{id}")
    public String rejeterDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.REFUSEE_RESP);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Afficher formulaire demande congé responsable
    @GetMapping("/responsable/conge/demande")
    public String afficherFormulaireDemande(Model model, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("matricule", responsable.getMatricule());
        model.addAttribute("nomPrenom", responsable.getNom() + " " + responsable.getPrenom());
        model.addAttribute("departementId", responsable.getDepartement().getId());
        model.addAttribute("role", responsable.getRole());

        return "demandeConge-responsable";
    }

    // Soumission demande congé responsable
    @PostMapping("/responsable/conge/demande/soumettre")
    public String soumettreDemandeCongeResponsable(@ModelAttribute DemandeConge demandeConge, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        demandeConge.setDemandeur(responsable);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE);
        demandeConge.setDateSoumission(LocalDate.now());

        demandeCongeRepository.save(demandeConge);

        return "redirect:/dashboard-responsable";
    }

    // Afficher demandes approuvées par responsable pour ce département (statut
    // APPROUVEE_RESP)
    // À noter : cette page est plutôt dédiée au RH, il serait mieux de créer un
    // RhController
    // Mais on garde ici pour l'instant, à adapter selon ta structure
    @GetMapping("/demandes-approuvees")
    public String afficherDemandesApprouvees(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        Departement departement = responsable.getDepartement();

        List<DemandeConge> demandesApprouvees = demandeCongeRepository.findDemandesByDepartementAndStatut(
                departement, StatutDemande.APPROUVEE_RESP);

        model.addAttribute("demandes", demandesApprouvees);
        model.addAttribute("activePage", "demandesApprouvees");
        // Vue différente de RH si tu veux : "demandesApprouveesResponsable"
        return "demandesApprouveesResponsable";
    }

    // Afficher profil responsable
    @GetMapping("/profil-responsable")
    public String afficherProfil(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null) {
            return "redirect:/login";
        }
        model.addAttribute("utilisateur", responsable);
        return "profil-responsable";
    }

    @GetMapping("/responsable/historique-demandes")
    public String afficherHistoriqueResponsable(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");

        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        // Statuts pertinents pour Responsable (demandes approuvées/refusées par le
        // responsable)
        List<StatutDemande> statutsHistoriqueResp = List.of(
                StatutDemande.APPROUVEE_RESP,
                StatutDemande.REFUSEE_RESP);

        List<DemandeConge> demandesHistorique = demandeCongeRepository.findByStatutIn(statutsHistoriqueResp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DemandeConge demande : demandesHistorique) {
            if (demande.getDateSoumission() != null) {
                demande.setDateSoumissionFormatee(demande.getDateSoumission().format(formatter));
            }
            // Tu peux aussi formatter dateDebut et dateFin ici si tu veux
            if (demande.getDateDebut() != null) {
                demande.setDateDebutFormatee(demande.getDateDebut().format(formatter));
            }
            if (demande.getDateFin() != null) {
                demande.setDateFinFormatee(demande.getDateFin().format(formatter));
            }
        }

        model.addAttribute("demandes", demandesHistorique);
        model.addAttribute("activePage", "historiqueDemandesResponsable");

        return "historique-demandes-responsable";
    }

}

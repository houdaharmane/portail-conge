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
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        if (!("RESPONSABLE".equals(utilisateur.getRole()) || "PERSONNEL".equals(utilisateur.getRole()))) {
            return "redirect:/login";
        }

        int nombrePersonnels = utilisateurService.getNombrePersonnels();
        int congesEnAttente = congeService.getCongesEnAttente();
        int congesValides = congeService.getCongesValides();

        model.addAttribute("totalPersonnel", nombrePersonnels);
        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValides", congesValides);
        model.addAttribute("utilisateur", utilisateur);

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
        demandeCongeRepository.save(demandeConge);

        return "redirect:/conges/confirmation";
    }

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

    // Approuver une demande
    @GetMapping("/approuver/{id}")
    public String approuverDemande(@PathVariable Long id) {
        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.APPROUVEE);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Rejeter une demande
    @GetMapping("/rejeter/{id}")
    public String rejeterDemande(@PathVariable Long id) {
        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.REJETEE);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Affichage formulaire demande congé responsable
    @GetMapping("/responsable/conge/demande")
    public String afficherFormulaireDemande(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"RESPONSABLE".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }
        // Ajouter les attributs nécessaires au formulaire, ex:
        model.addAttribute("matricule", utilisateur.getMatricule());
        model.addAttribute("nomPrenom", utilisateur.getNom() + " " + utilisateur.getPrenom());
        model.addAttribute("departementId", utilisateur.getDepartement().getId());
        model.addAttribute("fonction", "RESPONSABLE");

        return "demandeConge-responsable";
    }

    // Soumission demande congé responsable
    @PostMapping("/responsable/conge/demande/soumettre")
    public String soumettreDemandeCongeResponsable(@ModelAttribute DemandeConge demandeConge, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"RESPONSABLE".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }
        demandeConge.setDemandeur(utilisateur);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE);
        demandeConge.setDateSoumission(LocalDate.now());

        demandeCongeRepository.save(demandeConge);

        return "redirect:/dashboard-responsable";
    }

}

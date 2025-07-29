package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
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

        model.addAttribute("totalPersonnel", 120);
        model.addAttribute("congesEnAttente", 8);
        model.addAttribute("congesValides", 52);
        model.addAttribute("utilisateur", utilisateur);

        return "dashboard-responsable";
    }

    // Soumission demande
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
}

package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ResponsableController {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

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

    @GetMapping("/responsable/demandes-a-traiter")
    public String demandesATraiter(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null) {
            return "redirect:/login";
        }

        Departement dep = responsable.getDepartement();

        List<DemandeConge> demandes = demandeCongeRepository
                .findByStatutAndDemandeur_Departement(StatutDemande.EN_ATTENTE, dep);

        model.addAttribute("demandes", demandes);
        return "demandes-reponsable";
    }
}

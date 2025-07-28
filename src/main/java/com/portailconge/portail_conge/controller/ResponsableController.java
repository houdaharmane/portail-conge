package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Utilisateur;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResponsableController {

    @GetMapping("/dashboard-responsable")
    public String dashboardResponsable(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        // Accepter aussi les rôles PERSONNEL ici
        if (!("RESPONSABLE".equals(utilisateur.getRole()) || "PERSONNEL".equals(utilisateur.getRole()))) {
            return "redirect:/login";
        }

        // Ajouter les données pour l'affichage du dashboard
        model.addAttribute("totalPersonnel", 120);
        model.addAttribute("congesEnAttente", 8);
        model.addAttribute("congesValides", 52);

        model.addAttribute("utilisateur", utilisateur);

        return "dashboard-responsable";
    }

}

package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RhController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/dashboard-rh")
    public String rhDashboard(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || rh.getRole() != Utilisateur.Role.RH) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        model.addAttribute("email", rh.getEmail());
        model.addAttribute("role", rh.getRole().name());
        return "dashboard-rh";
    }

    @GetMapping("/profil")
    public String afficherProfilRH(HttpSession session, Model model,
            @RequestParam(required = false) Boolean modification) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || rh.getRole() != Utilisateur.Role.RH) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        model.addAttribute("modification", modification != null && modification);

        return "profil";
    }

    @GetMapping("/profil/modifier")
    public String afficherFormulaireModificationProfil(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

        if (utilisateur == null || utilisateur.getRole() != Utilisateur.Role.RH) {
            return "redirect:/login";
        }

        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("modification", true);
        return "modifier_profil";
    }

    @PostMapping("/profil/save")
    public String enregistrerProfil(@ModelAttribute Utilisateur utilisateurModifie, HttpSession session) {
        Utilisateur utilisateurSession = (Utilisateur) session.getAttribute("utilisateur");

        if (utilisateurSession == null || utilisateurSession.getRole() != Utilisateur.Role.RH) {
            return "redirect:/login";
        }

        utilisateurSession.setEmail(utilisateurModifie.getEmail());
        utilisateurSession.setMotDePasse(utilisateurModifie.getMotDePasse());

        utilisateurRepository.save(utilisateurSession);
        session.setAttribute("utilisateur", utilisateurSession);

        return "redirect:/profil";
    }
}

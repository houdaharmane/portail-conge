package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import com.portailconge.portail_conge.repository.DepartementRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RhController {

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/dashboard-rh")
    public String rhDashboard(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        model.addAttribute("email", rh.getEmail());
        model.addAttribute("role", rh.getRole());
        model.addAttribute("activePage", "dashboard");
        return "dashboard-rh";
    }

    @GetMapping("/profil")
    public String afficherProfilRH(HttpSession session, Model model,
            @RequestParam(required = false) Boolean modification) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        model.addAttribute("modification", modification != null && modification);
        model.addAttribute("departements", departementRepository.findAll()); // liste des départements

        model.addAttribute("activePage", "profil");
        return "profil";
    }

    @GetMapping("/profil/modifier")
    public String afficherFormulaireModificationProfil(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("utilisateur", rh);
        model.addAttribute("activePage", "profil");
        return "modifier_profil";
    }

    @PostMapping("/profil/save")
    public String enregistrerProfil(@ModelAttribute("utilisateur") Utilisateur utilisateurModifie,
            HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        try {
            rh.setEmail(utilisateurModifie.getEmail());
            if (utilisateurModifie.getMotDePasse() != null && !utilisateurModifie.getMotDePasse().isEmpty()) {
                rh.setMotDePasse(utilisateurModifie.getMotDePasse());
            }
            rh.setNom(utilisateurModifie.getNom());
            rh.setPrenom(utilisateurModifie.getPrenom());
            rh.setTelephone(utilisateurModifie.getTelephone());
            rh.setCin(utilisateurModifie.getCin());
            rh.setSoldeConge(utilisateurModifie.getSoldeConge());

            // Récupération du département sélectionné (en supposant que ton formulaire
            // envoie departement.id)
            if (utilisateurModifie.getDepartement() != null && utilisateurModifie.getDepartement().getId() != null) {
                departementRepository.findById(utilisateurModifie.getDepartement().getId())
                        .ifPresent(rh::setDepartement);
            } else {
                rh.setDepartement(null);
            }

            utilisateurRepository.save(rh);
            session.setAttribute("utilisateur", rh);

            return "redirect:/profil?modification=true";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la mise à jour du profil.");
            model.addAttribute("utilisateur", rh);
            model.addAttribute("activePage", "profil");
            return "modifier_profil";
        }
    }

}

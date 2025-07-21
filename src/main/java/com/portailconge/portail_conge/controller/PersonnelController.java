package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PersonnelController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/personnel/ajouter")
    public String afficherFormulaireAjout(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || rh.getRole() != Utilisateur.Role.RH) {
            return "redirect:/login";
        }
        model.addAttribute("rh", rh);
        return "ajouter-personnel";
    }

    @PostMapping("/personnel/ajouter")
    public String ajouterPersonnel(@RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam String role,
            HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || rh.getRole() != Utilisateur.Role.RH) {
            return "ajouter-personnel";
        }

        Utilisateur.Role roleEnum;
        try {
            roleEnum = Utilisateur.Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return "redirect:/personnel/ajouter?error=role";
        }

        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setNom(nom);
        nouvelUtilisateur.setPrenom(prenom);
        nouvelUtilisateur.setEmail(email);
        nouvelUtilisateur.setRole(roleEnum);

        utilisateurRepository.save(nouvelUtilisateur);

        return "redirect:/personnel/ajouter?success";
    }
}

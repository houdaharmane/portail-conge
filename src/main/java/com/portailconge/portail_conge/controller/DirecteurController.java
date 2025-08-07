package com.portailconge.portail_conge.controller;

import java.io.IOException;
import java.util.Optional;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DepartementRepository;
import com.portailconge.portail_conge.repository.UtilisateurRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DirecteurController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @GetMapping("/dashboard-directeur")
    public String dashboardDirecteur(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"DIRECTEUR".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("directeur", utilisateur);
        model.addAttribute("activePage", "dashboard");
        session.setAttribute("utilisateurConnecteId", utilisateur.getId());

        return "dashboard-directeur";
    }

    @GetMapping("/profil-directeur")
    public String afficherProfilDirecteur(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"DIRECTEUR".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("directeur", utilisateur);
        model.addAttribute("departements", departementRepository.findAll());

        return "profil-directeur";
    }

    @PostMapping("/profil-directeur/save")
    public String sauvegarderProfilDirecteur(
            @ModelAttribute Utilisateur directeur,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(name = "confirm_password", required = false) String confirmPassword,
            HttpSession session,
            Model model) throws IOException {
        System.out.println("Reception formulaire: nom=" + directeur.getNom() + ", prenom=" + directeur.getPrenom());

        Integer utilisateurId = (Integer) session.getAttribute("utilisateurConnecteId");
        if (utilisateurId == null) {
            return "redirect:/login";
        }

        Optional<Utilisateur> optUser = utilisateurRepository.findById(utilisateurId);
        if (optUser.isEmpty()) {
            return "redirect:/login";
        }

        Utilisateur utilisateurEnBase = optUser.get();

        // Mise à jour du mot de passe si présent
        if (directeur.getMotDePasse() != null && !directeur.getMotDePasse().isEmpty()) {
            if (!directeur.getMotDePasse().equals(confirmPassword)) {
                model.addAttribute("error", "Les mots de passe ne correspondent pas");
                model.addAttribute("directeur", utilisateurEnBase);
                model.addAttribute("departements", departementRepository.findAll());
                return "profil-directeur";
            }
            utilisateurEnBase.setMotDePasse(directeur.getMotDePasse());
        }

        // Mise à jour des infos personnelles
        utilisateurEnBase.setNom(directeur.getNom());
        utilisateurEnBase.setPrenom(directeur.getPrenom());
        utilisateurEnBase.setTelephone(directeur.getTelephone());
        utilisateurEnBase.setEmail(directeur.getEmail());
        utilisateurEnBase.setCin(directeur.getCin());

        // Mise à jour du département
        if (directeur.getDepartement() != null && directeur.getDepartement().getId() != null) {
            departementRepository.findById(directeur.getDepartement().getId())
                    .ifPresent(utilisateurEnBase::setDepartement);
        } else {
            utilisateurEnBase.setDepartement(null);
        }

        // Traitement de l'image
        if (imageFile != null && !imageFile.isEmpty()) {
            utilisateurEnBase.setPhoto(imageFile.getBytes());
        }

        utilisateurRepository.save(utilisateurEnBase);
        session.setAttribute("utilisateur", utilisateurEnBase);

        return "redirect:/profil-directeur";
    }
}

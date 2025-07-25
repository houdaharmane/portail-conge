package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.Personnel;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DepartementRepository;
import com.portailconge.portail_conge.service.AuthService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/personnel")
public class PersonnelController {

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/ajouter")
    public String afficherFormulaireAjout(Model model, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        model.addAttribute("departements", departementRepository.findAll());

        model.addAttribute("personnel", new Personnel());

        return "ajouter-personnel";
    }

    @PostMapping("/ajouter")
    public String ajouterPersonnel(
            @RequestParam String matricule,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam("numero") String telephone,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam Integer departement,
            @RequestParam String cin,
            @RequestParam String motDePasse,
            @RequestParam Integer soldeConge,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        try {
            // Créer l'utilisateur
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setMatricule(matricule);
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setTelephone(telephone);
            utilisateur.setEmail(email);
            utilisateur.setRole(role);

            Departement dept = new Departement();
            dept.setId(departement.longValue());
            utilisateur.setDepartement(dept);

            utilisateur.setCin(cin);
            utilisateur.setMotDePasse(motDePasse);
            utilisateur.setSoldeConge(soldeConge);

            // Créer le personnel
            Personnel personnel = new Personnel();
            personnel.setNom(nom);
            personnel.setPrenom(prenom);
            personnel.setMatricule(matricule);
            personnel.setTelephone(telephone);
            personnel.setEmail(email);
            personnel.setCin(cin);

            personnel.setDepartementId(departement);

            personnel.setUtilisateur(utilisateur); // liaison

            authService.creerUtilisateurEtPersonnel(utilisateur, personnel);

            redirectAttributes.addFlashAttribute("success", "Personnel ajouté avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout du personnel.");
        }

        return "redirect:/personnel/ajouter";

    }

    // Interface personnel : tableau de bord
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "dashboard-personnel";
    }

    // Interface personnel : profil
    @GetMapping("/profil")
    public String showProfil(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        System.out.println("Session utilisateur : " + utilisateur);

        if (utilisateur == null) {
            System.out.println("Utilisateur null, redirection vers login");

            return "redirect:/login";
        }
        model.addAttribute("activePage", "profil");

        session.setAttribute("utilisateur", utilisateur);
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("personnelrole", utilisateur.getRole());
        return "profil-personnel";
    }

    @GetMapping("/test-session")
    @ResponseBody
    public String testSession(HttpSession session) {
        Object utilisateur = session.getAttribute("utilisateur");
        return utilisateur != null ? "Session OK: " + utilisateur.toString() : "Session vide";
    }

}
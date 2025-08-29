package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import com.portailconge.portail_conge.service.CongeService;
import com.portailconge.portail_conge.model.DemandeConge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CongeService congeService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

        if (utilisateur == null || !utilisateur.getMotDePasse().equals(password)) {
            model.addAttribute("error", "Email ou mot de passe incorrect !");
            return "login";
        }

        // Charger l'utilisateur complet depuis la base pour éviter les valeurs null
        utilisateur = utilisateurRepository.findById(utilisateur.getId()).orElse(null);
        if (utilisateur == null) {
            model.addAttribute("error", "Utilisateur introuvable !");
            return "login";
        }

        session.setAttribute("utilisateur", utilisateur);

        // Redirection vers le dashboard principal selon le rôle
        switch (utilisateur.getRole()) {
            case "PERSONNEL":
                return "redirect:/dashboard";
            case "RH":
                return "redirect:/dashboard-rh";
            case "RESPONSABLE":
                return "redirect:/dashboard-responsable";
            case "DIRECTEUR":
                return "redirect:/dashboard-directeur";
            default:
                model.addAttribute("error", "Rôle inconnu !");
                return "login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        // Récupérer les congés de l'utilisateur
        List<DemandeConge> conges = congeService.getCongesByUtilisateur(utilisateur);
        int annee = Year.now().getValue();

        // Calcul des totaux
        int congesConsommes = congeService.calculerJoursPrisConfirmes(utilisateur, annee);
        int soldeDisponible = congeService.calculerSoldeTotalDisponible(utilisateur, annee);
        int congesAnnuels = 30;

        // Formater les dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DemandeConge d : conges) {
            if (d.getDateDebut() != null)
                d.setDateDebutFormatee(d.getDateDebut().format(dateFormatter));
            if (d.getDateFin() != null)
                d.setDateFinFormatee(d.getDateFin().format(dateFormatter));
            if (d.getDateSoumission() != null)
                d.setDateSoumissionFormatee(d.getDateSoumission().format(dateTimeFormatter));
        }

        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("conges", conges);
        model.addAttribute("congesAnnuels", congesAnnuels);
        model.addAttribute("congesConsommes", congesConsommes);
        model.addAttribute("soldeTotal", soldeDisponible);
        model.addAttribute("activePage", "dashboard");

        return "dashboard-personnel";
    }

    @GetMapping("/profil-personnel")
    public String afficherProfil(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }
        model.addAttribute("utilisateur", utilisateur);
        return "profil-personnel";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}

package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import com.portailconge.portail_conge.service.CongeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        session.setAttribute("utilisateur", utilisateur);

        switch (utilisateur.getRole()) {
            case "PERSONNEL":
                return "redirect:/dashboard-personnel";
            case "RESPONSABLE":
                if (utilisateur.getDepartement().getId() == 1) { // département RH
                    return "redirect:/dashboard-rh";
                } else {
                    return "redirect:/dashboard-responsable";
                }

            case "DIRECTEUR":
                return "redirect:/dashboard-directeur";
            default:
                model.addAttribute("error", "Rôle inconnu !");
                return "login";
        }
    }

    @GetMapping("/dashboard-personnel")
    public String showDashboardPersonnel(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null)
            return "redirect:/login";

        List<DemandeConge> conges = congeService.getCongesByUtilisateur(utilisateur);
        int annee = Year.now().getValue();

        int congesConsommes = congeService.calculerJoursPrisConfirmes(utilisateur, annee);
        int soldeDisponible = congeService.calculerSoldeTotalDisponible(utilisateur, annee);
        int congesAnnuels = 30;

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
        if (utilisateur == null)
            return "redirect:/login";

        model.addAttribute("utilisateur", utilisateur);
        return "profil-personnel";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}

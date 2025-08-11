package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

        if (utilisateur == null) {
            model.addAttribute("error", "Email ou mot de passe incorrect !");
            return "login";
        }

        if (utilisateur.getMotDePasse().equals(password)) {
            String role = utilisateur.getRole();

            if (role == null) {
                model.addAttribute("error", "Le rôle de l'utilisateur n'est pas défini, contactez l'administrateur.");
                return "login";
            }
            session.setAttribute("utilisateur", utilisateur);

            switch (role) {
                case "PERSONNEL":
                    return "redirect:/dashboard-personnel";
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
        } else {
            model.addAttribute("error", "Email ou mot de passe incorrect !");
            return "login";
        }
    }

    @GetMapping("/dashboard-personnel")
    public String dashboardPersonnel(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }
        model.addAttribute("utilisateur", utilisateur);
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
        session.invalidate(); // détruire la session utilisateur
        return "redirect:/login?logout";
    }

}

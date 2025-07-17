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
    public String login(@RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

        if (utilisateur != null && utilisateur.getMotDePasse().equals(password)) {
            session.setAttribute("user", utilisateur);

            switch (utilisateur.getRole()) {
                case PERSONNEL:
                    return "redirect:/dashboard-personnel";
                case RH:
                    return "redirect:/dashboard-rh";
                case RESPONSABLE:
                    return "redirect:/dashboard-responsable";
                case ADMISSION:
                    return "redirect:/dashboard-admission";
                case DIRECTEUR:
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
}

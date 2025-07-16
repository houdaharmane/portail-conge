package com.portailconge.portail_conge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    // Affiche la page de login
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // Traite le formulaire de login
    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        if ("user@gmail.com".equals(email) && "123".equals(password)) {
            session.setAttribute("user", email);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Email ou mot de passe incorrect !");
            return "login";
        }
    }
}

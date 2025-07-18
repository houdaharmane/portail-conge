
package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Utilisateur;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RhController {

    @GetMapping("/dashboard-rh")
    public String rhDashboard(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("user");

        if (rh == null || rh.getRole() != Utilisateur.Role.RH) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        return "dashboard-rh";
    }
}

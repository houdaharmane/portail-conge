package com.portailconge.portail_conge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.model.Personnel;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import com.portailconge.portail_conge.repository.PersonnelRepository;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PersonnelRepository personnelRepository;

    @Transactional
    public void creerUtilisateurEtPersonnel(Utilisateur utilisateur, Personnel personnel) {
        Utilisateur savedUser = utilisateurRepository.save(utilisateur);
        personnel.setUtilisateur(savedUser);
        personnel.setMatricule(utilisateur.getMatricule());
        personnelRepository.save(personnel);
    }

}

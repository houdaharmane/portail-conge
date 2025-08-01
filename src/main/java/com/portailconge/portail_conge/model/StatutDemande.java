package com.portailconge.portail_conge.model;

public enum StatutDemande {
    REFUSEE, // demande refusée
    EN_ATTENTE, // demande soumise par le personnel
    APPROUVEE_RESP, // approuvée par responsable (en attente de RH)
    REFUSEE_RESP, // refusée par responsable
    APPROUVEE_RH, // approuvée par RH (en attente du directeur)
    REFUSEE_RH, // refusée par RH
    APPROUVEE_DIRECTEUR, // approuvée par directeur (validée à 100%)
    REFUSEE_DIRECTEUR // refusée par directeur
}

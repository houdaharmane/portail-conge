package com.portailconge.portail_conge.model;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    public static byte[] generateCongePdf(DemandeConge demande, String titrePdf) throws Exception {
        if (demande.getStatut() != StatutDemande.APPROUVEE_DIRECTEUR) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Logo
        String logoPath = "src/main/resources/static/img/modepbg.png";
        try {
            Image logo = new Image(ImageDataFactory.create(logoPath)).scaleToFit(100, 100);
            document.add(logo);
        } catch (Exception e) {
            System.out.println("Logo non trouvé : " + logoPath);
        }
        document.add(new Paragraph("\n"));

        // Titre dynamique
        Paragraph title = new Paragraph(titrePdf).setBold().setFontSize(18);
        document.add(title.setMarginLeft(150));
        document.add(new Paragraph("\n"));

        // Nom et prénom
        if (demande.getDemandeur() != null) {
            document.add(new Paragraph("Nom : " + demande.getDemandeur().getNom()));
            document.add(new Paragraph("Prénom : " + demande.getDemandeur().getPrenom()));
        } else {
            document.add(new Paragraph("Nom : "));
            document.add(new Paragraph("Prénom : "));
        }

        // Département
        String dept = "";
        if (demande.getDepartement() != null) {
            dept = demande.getDepartement().getNom();
        } else if (demande.getDemandeur() != null && demande.getDemandeur().getDepartement() != null) {
            dept = demande.getDemandeur().getDepartement().getNom();
        }
        document.add(new Paragraph("Département : " + dept));

        // Fonction
        String fonction = "";
        if (demande.getFonction() != null && !demande.getFonction().isEmpty()) {
            fonction = demande.getFonction();
        } else if (demande.getDemandeur() != null) {
            fonction = demande.getDemandeur().getRole();
        }
        document.add(new Paragraph("Fonction : " + fonction));

        // Dates et durée
        document.add(new Paragraph("Date début : " + demande.getDateDebut()));
        document.add(new Paragraph("Date fin : " + demande.getDateFin()));
        document.add(new Paragraph("Durée : " + demande.getDuree() + " jours"));

        // Date du jour
        String dateDuJour = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        document.add(new Paragraph("Date : " + dateDuJour));

        // Statut
        document.add(new Paragraph("Statut : " + demande.getStatut()));

        document.close();
        return baos.toByteArray();
    }
}

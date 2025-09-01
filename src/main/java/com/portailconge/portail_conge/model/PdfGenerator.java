package com.portailconge.portail_conge.model;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;

public class PdfGenerator {

    public static byte[] generateCongePdf(DemandeConge demande, String titrePdf) throws Exception {
        if (demande == null) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

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
        document.add(
                new Paragraph("Est autorisé à prendre un congé administratif de : " + demande.getDuree() + " jours"));

        document.close();
        return baos.toByteArray();
    }
}

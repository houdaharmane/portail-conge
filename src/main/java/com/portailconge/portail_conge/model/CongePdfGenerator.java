package com.portailconge.portail_conge.model;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CongePdfGenerator {

        /**
         * Génère la fiche PDF d'une demande de congé pour un utilisateur spécifique.
         *
         * @param demande     La demande de congé
         * @param utilisateur L'utilisateur qui signe actuellement la fiche (Personnel,
         *                    Responsable ou Directeur)
         * @return tableau de bytes représentant le PDF
         * @throws Exception
         */
        public byte[] genererFichePdf(DemandeConge demande, Utilisateur utilisateur) throws Exception {
                if (demande == null || demande.getDemandeur() == null) {
                        throw new RuntimeException("Demande ou demandeur est null");
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // ====== Titre principal ======
                Paragraph titre = new Paragraph("DEMANDE DE CONGE")
                                .setBold()
                                .setFontSize(16)
                                .setTextAlignment(TextAlignment.CENTER);
                document.add(titre);
                document.add(new Paragraph("\n"));

                // ====== Bloc Informations Personnel ======
                Div infoDiv = new Div()
                                .setPadding(15)
                                .setBorder(new SolidBorder(1))
                                .setMarginBottom(20);

                // Nom et prénom du demandeur
                String nomPrenom = ((demande.getDemandeur().getNom() != null ? demande.getDemandeur().getNom() : "-")
                                + " " +
                                (demande.getDemandeur().getPrenom() != null ? demande.getDemandeur().getPrenom() : "-"))
                                .trim();

                // Département selon l'utilisateur qui signe
                String departement = (utilisateur.getDepartement() != null
                                && utilisateur.getDepartement().getNom() != null)
                                                ? utilisateur.getDepartement().getNom()
                                                : "-";

                String infos = "Matricule : "
                                + (demande.getDemandeur().getMatricule() != null ? demande.getDemandeur().getMatricule()
                                                : "-")
                                + "\n" +
                                "Nom et Prénom : " + nomPrenom + "\n" +
                                "Département: " + departement + "\n" +
                                "Durée demandée : " + (demande.getDuree() > 0 ? demande.getDuree() + " jours" : "-")
                                + "\n" +
                                "Du : "
                                + (demande.getDateDebut() != null ? demande.getDateDebut().format(formatter) : "-")
                                + "\n" +
                                "Au : " + (demande.getDateFin() != null ? demande.getDateFin().format(formatter) : "-");

                Paragraph infoParag = new Paragraph(infos)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.LEFT);
                infoDiv.add(infoParag);

                // Lieu et date
                Paragraph lieuDate = new Paragraph(
                                (demande.getLieuDate() != null ? demande.getLieuDate() : "Casablanca")
                                                + ", le " + LocalDate.now().format(formatter))
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setMarginTop(10);
                infoDiv.add(lieuDate);

                // Signature selon rôle de l'utilisateur
                if (utilisateur.getSignatureImagePath() != null) {
                        Image signImg = new Image(ImageDataFactory.create(utilisateur.getSignatureImagePath()))
                                        .setWidth(120)
                                        .setAutoScale(true)
                                        .setHorizontalAlignment(
                                                        com.itextpdf.layout.properties.HorizontalAlignment.RIGHT)
                                        .setMarginTop(10);
                        infoDiv.add(signImg);
                }

                if ("PERSONNEL".equals(utilisateur.getRole()) || "DIRECTEUR".equals(utilisateur.getRole())) {

                }

                document.add(infoDiv);

                // ====== Bloc Responsable ======
                Div respDiv = new Div()
                                .setPadding(15)
                                .setBorder(new SolidBorder(1))
                                .setMarginBottom(20);

                Paragraph respTitre = new Paragraph("A REMPLIR PAR LE RESPONSABLE DIRECT")
                                .setBold()
                                .setFontSize(13)
                                .setTextAlignment(TextAlignment.LEFT);
                respDiv.add(respTitre);

                Paragraph avisResp = new Paragraph("Avis du responsable direct :\n\nIntérimaire proposé :")
                                .setFontSize(12)
                                .setMarginTop(10);
                respDiv.add(avisResp);

                if ("RESPONSABLE".equals(utilisateur.getRole()) && utilisateur.getSignatureImagePath() != null) {
                        Image signImg = new Image(ImageDataFactory.create(utilisateur.getSignatureImagePath()))
                                        .setWidth(120)
                                        .setAutoScale(true)
                                        .setHorizontalAlignment(
                                                        com.itextpdf.layout.properties.HorizontalAlignment.RIGHT)
                                        .setMarginTop(10);
                        respDiv.add(signImg);

                }

                document.add(respDiv);

                // ====== Bloc Directeur ======
                Div dirDiv = new Div()
                                .setPadding(15)
                                .setBorder(new SolidBorder(1));

                Paragraph dirTitre = new Paragraph("DECISION DU DIRECTEUR")
                                .setBold()
                                .setFontSize(13)
                                .setTextAlignment(TextAlignment.LEFT);
                dirDiv.add(dirTitre);

                Paragraph avisDir = new Paragraph("Avis :")
                                .setFontSize(12)
                                .setMarginTop(10);
                dirDiv.add(avisDir);

                if ("DIRECTEUR".equals(utilisateur.getRole()) && utilisateur.getSignatureImagePath() != null) {
                        Image signImg = new Image(ImageDataFactory.create(utilisateur.getSignatureImagePath()))
                                        .setWidth(120)
                                        .setAutoScale(true)
                                        .setHorizontalAlignment(
                                                        com.itextpdf.layout.properties.HorizontalAlignment.RIGHT)
                                        .setMarginTop(10);
                        dirDiv.add(signImg);

                }

                document.add(dirDiv);

                document.close();
                return baos.toByteArray();
        }
}

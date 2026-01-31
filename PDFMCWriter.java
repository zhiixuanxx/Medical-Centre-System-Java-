package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.FileOutputStream;

import model.Appointment;
import model.Doctor;
import model.MedicalCertificate;

public class PDFMCWriter {

    public static void writeMCPdf(String filePath, Appointment appt, Doctor doctor, MedicalCertificate mc) throws Exception {

        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        // Fonts
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font subFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

        // Header
        Paragraph brand = new Paragraph("APU Medical Centre", titleFont);
        brand.setAlignment(Element.ALIGN_CENTER);
        doc.add(brand);

        Paragraph sub = new Paragraph("Jalan Teknologi 5, 57000 Kuala Lumpur • +60 3-1234 5678\n\n", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);

        Paragraph header = new Paragraph("MEDICAL CERTIFICATE\n\n", labelFont);
        header.setAlignment(Element.ALIGN_CENTER);
        doc.add(header);

        // Info table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        addRow(table, "Appointment ID:", mc.getAppointmentId(), labelFont, valueFont);
        addRow(table, "Issue Date:", mc.getIssueDate(), labelFont, valueFont);
        addRow(table, "Patient:", appt.getCustomerName(), labelFont, valueFont);
        addRow(table, "Doctor:", formatDoctorDisplayName(doctor.getName()), labelFont, valueFont);
        addRow(table, "Rest From:", mc.getRestFrom(), labelFont, valueFont);
        addRow(table, "Rest To:", mc.getRestTo(), labelFont, valueFont);
        addRow(table, "Remarks:", (mc.getRemarks() == null || mc.getRemarks().isBlank()) ? "-" : mc.getRemarks(), labelFont, valueFont);

        doc.add(table);

        // Signature section
        doc.add(new Paragraph("\n\n"));
        Font signLabel = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
        BaseFont bf = BaseFont.createFont("src/resources/DancingScript.ttf",BaseFont.IDENTITY_H,BaseFont.EMBEDDED
        );
        Font signFont = new Font(bf, 24, Font.NORMAL, Color.BLUE);
        
        String cleanName = doctor.getName().replaceAll("(?i)^\\s*dr\\.?\\s*", "").trim();

        Paragraph signPara = new Paragraph(cleanName, signFont);
        signPara.setAlignment(Element.ALIGN_LEFT);
        doc.add(signPara);

        Paragraph line = new Paragraph("______________________________", valueFont);
        line.setAlignment(Element.ALIGN_LEFT);
        doc.add(line);

        Paragraph signText = new Paragraph("Doctor's Signature", signLabel);
        signText.setAlignment(Element.ALIGN_LEFT);
        doc.add(signText);

        doc.close();
    }

    // Helper for adding rows
    private static void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(5);
        c2.setPadding(5);
        table.addCell(c1);
        table.addCell(c2);
    }

    // Format Doctor name (avoid double Dr.)
    private static String formatDoctorDisplayName(String name) {
        if (name == null) return "Dr. Unknown";
        String clean = name.replaceAll("(?i)^\\s*dr\\.?\\s*", "").trim();
        return "Dr. " + (clean.isEmpty() ? "Unknown" : clean);
    }

}
